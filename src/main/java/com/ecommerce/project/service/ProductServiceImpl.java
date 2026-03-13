package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService{
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final FileService fileService;
    private final CartRepository cartRepository;
    private final CartService cartService;

    @Value("${project.image.upload.path}")
    private String imageUploadPath;

    @Override
    @CacheEvict(value = {"product-list", "product-search"}, allEntries = true)
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO, User user) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() ->
                new ResourceNotFoundException("Category", "categoryId", categoryId));

        Product product = modelMapper.map(productDTO, Product.class);

        List<Product> productList = category.getProducts();
        for(Product retrievedProduct: productList){
            if(retrievedProduct.getProductName().equals(product.getProductName()))
                throw new APIException("Product with name{" + product.getProductName()
                + "} already exists in category{" + categoryId + "}");
        }

        product.setImage("default.png");
        product.setCategory(category);
        product.setUser(user);

        Double specialPrice = product.getPrice() -
                (product.getDiscount() * 0.01 * product.getPrice());
        product.setSpecialPrice(specialPrice);

        Product savedProduct = productRepository.save(product);

        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    public ProductResponse convertProductPageToProductResponse(int pageNumber, int pageSize, Page<Product> productPage){
        List<Product> products = productPage.getContent();

        List<ProductDTO> productDTOs = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOs);
        productResponse.setPageNumber(pageNumber);
        productResponse.setPageSize(pageSize);
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setLastPage(productPage.isLast());

        return productResponse;
    }

    @Override
    @Cacheable(value = "product-list", key = "{#pageNumber, #pageSize, #sortBy, #sortOrder}", condition = "#pageNumber == 0")
    public ProductResponse getAllProducts(int pageNumber, int pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable productPageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<Product> productPage = productRepository.findAll(productPageDetails);

        return convertProductPageToProductResponse(pageNumber, pageSize, productPage);
    }


    @Override
    @Cacheable(value = "product-list", key = "{#categoryId, #pageNumber, #pageSize, #sortBy, #sortOrder}", condition = "#pageNumber == 0")
    public ProductResponse getProductsByCategory(Long categoryId, int pageNumber, int pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable productPageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category", "categoryId", categoryId));

        Page<Product> productPage = productRepository.findByCategoryOrderByPriceAsc(category, productPageDetails);
        return convertProductPageToProductResponse(pageNumber, pageSize, productPage);
    }

    @Override
    @Cacheable(
            value = "product-search",
            key = "{#keyword, #pageNumber, #pageSize, #sortBy, #sortOrder}",
            condition = "#keyword.toLowerCase() matches 'iphone|samsung|laptop|shoes|chocolate'"
    )
    public ProductResponse searchProductsByKeyword(String keyword, int pageNumber, int pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable productPageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<Product> productPage = productRepository.findByProductNameContainingIgnoreCase(keyword, productPageDetails);

        return convertProductPageToProductResponse(pageNumber, pageSize, productPage);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = {"product-list", "product-search"}, allEntries = true),
            @CacheEvict(value = "product", key = "#productId")
    })
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        Product productFromDb = productRepository.findById(productId).orElseThrow(() ->
                new ResourceNotFoundException("Product", "productId", productId));

        Product product = modelMapper.map(productDTO, Product.class);

        Double specialPrice = product.getPrice() -
                (product.getDiscount() * 0.01 * product.getPrice());
        product.setSpecialPrice(specialPrice);

        productFromDb.setProductName(product.getProductName());
        productFromDb.setDiscount(product.getDiscount());
        productFromDb.setDescription(product.getDescription());
        productFromDb.setQuantity(product.getQuantity());
        productFromDb.setPrice(product.getPrice());
        productFromDb.setSpecialPrice(product.getSpecialPrice());

        Product savedProduct = productRepository.save(productFromDb);

        List<Cart> carts = cartRepository.findCartsByProduct(productId);

        carts.forEach(cart -> cartService.updateProductInCart(cart.getCartId(), productId));

        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = {"product-list", "product-search"}, allEntries = true),
            @CacheEvict(value = "product", key = "#productId")
    })
    public ProductDTO deleteProduct(Long productId) {
        Product productFromDb = productRepository.findById(productId).orElseThrow(() ->
                new ResourceNotFoundException("Product", "productId", productId));

        productRepository.delete(productFromDb);
        
        return modelMapper.map(productFromDb, ProductDTO.class);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = {"product-list", "product-search"}, allEntries = true),
            @CacheEvict(value = "product", key = "#productId")
    })
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        String fileName = fileService.uploadImage(imageUploadPath, image);

        productFromDb.setImage(fileName);

        Product savedProduct = productRepository.save(productFromDb);

        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    @Cacheable(value = "product", key = "#productId")
    public ProductDTO getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        return modelMapper.map(product, ProductDTO.class);
    }
}
