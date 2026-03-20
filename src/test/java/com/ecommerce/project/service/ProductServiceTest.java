package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private FileServiceImpl fileService;
    @Mock
    private CartService cartService;
    private final ModelMapper modelMapper = new ModelMapper();
    private ProductServiceImpl productService;

    @BeforeEach
    void setup(){
        productService = new ProductServiceImpl(categoryRepository, productRepository, modelMapper, fileService, cartRepository, cartService);
    }

    private void assertPageableHasSorting(int pageNumber, int pageSize, Sort.Direction sortOrder, String sortBy, Pageable pageable){ // This method verifies whether the Pageable object contains the correct sort parameters
        Assertions.assertEquals(pageNumber, pageable.getPageNumber());
        Assertions.assertEquals(pageSize, pageable.getPageSize());
        Sort.Order order = pageable.getSort().getOrderFor(sortBy);
        Assertions.assertEquals(sortOrder, order.getDirection());
    }

    private void setImageUploadPath(){
        ReflectionTestUtils.setField(productService, "imageUploadPath", "images");
    }

    private Category getTestCategory(Long categoryId){
        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setCategoryName("Tech");
        return category;
    }

    private ProductDTO getTestInputProductDTO(){
        ProductDTO inputProductDTO = new ProductDTO();
        inputProductDTO.setProductName("Hp Laptop");
        inputProductDTO.setQuantity(4);
        inputProductDTO.setDiscount(10.0);
        inputProductDTO.setDescription("Hp Laptop");
        inputProductDTO.setPrice(4499.0);

        return inputProductDTO;
    }

    private User getTestSellerUser(){
        User seller = new User();
        seller.setUserId(1L);
        seller.setEmail("seller@test.com");
        seller.setUsername("seller1");

        return seller;
    }

    private List<Product> getTestProductList(){
        Product product1 = new Product();
        product1.setProductName("Hp Laptop");

        Product product2 = new Product();
        product2.setProductName("Dell Laptop");

        return List.of(product1, product2);
    }

    @Test
    void addProductTest_ShouldPass_UniqueProductAddition(){
        Long categoryId = 1L;
        Category category = getTestCategory(categoryId);

        ProductDTO inputProductDTO = getTestInputProductDTO();

        User seller = getTestSellerUser();

        Mockito.when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));
        Mockito.when(productRepository.save(Mockito.any(Product.class)))
                .thenAnswer(invocation -> {
                    Product p = invocation.getArgument(0);
                    p.setProductId(1L);
                    return p;
                });

        ProductDTO savedProductDTO = productService.addProduct(categoryId, inputProductDTO, seller);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productRepository).save(productCaptor.capture());
        Product capturedProduct = productCaptor.getValue();

        Double expectedSpecialPrice = inputProductDTO.getPrice() -
                (inputProductDTO.getDiscount() * 0.01 * inputProductDTO.getPrice());

        Assertions.assertEquals(inputProductDTO.getProductName(), savedProductDTO.getProductName());
        Assertions.assertEquals("default.png", capturedProduct.getImage());
        Assertions.assertEquals(expectedSpecialPrice, capturedProduct.getSpecialPrice());
        Assertions.assertEquals(category, capturedProduct.getCategory());
        Assertions.assertEquals(seller, capturedProduct.getUser());
    }

    @Test
    void addProductTest_ShouldThrowException_DuplicateProductAddition(){
        Long categoryId = 1L;
        Category category = getTestCategory(categoryId);

        Product existingProduct = new Product();
        existingProduct.setProductName("Hp Laptop");
        category.getProducts().add(existingProduct);

        ProductDTO inputProductDTO = getTestInputProductDTO();

        User seller = getTestSellerUser();

        Mockito.when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        Assertions.assertThrows(APIException.class, () ->
            productService.addProduct(categoryId, inputProductDTO, seller));

        Mockito.verify(productRepository, Mockito.never())
                .save(Mockito.any(Product.class));
    }

    @Test
    public void addProductTest_ShouldThrowResourceNotFoundException_CategoryDoesntExist(){
        Long categoryId = 1L;

        ProductDTO inputProductDTO = getTestInputProductDTO();

        User seller = getTestSellerUser();

        Mockito.when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () ->
            productService.addProduct(categoryId, inputProductDTO, seller));

        Mockito.verify(productRepository, Mockito.never())
                .save(Mockito.any(Product.class));
    }

    @Test
    public void getAllProductsTest_ShouldReturnProductResponse_withAscendingSort(){
        int pageNumber = 0;
        int pageSize = 10;
        String sortBy = "price";
        String sortOrder = "asc";

        List<Product> productList = getTestProductList();

        Page<Product> productPage = new PageImpl<>(productList, PageRequest.of(pageNumber, pageSize), 2);

        Mockito.when(productRepository.findAll(Mockito.any(Pageable.class)))
                        .thenReturn(productPage);

        ProductResponse response = productService.getAllProducts(pageNumber, pageSize, sortBy, sortOrder);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        Mockito.verify(productRepository).findAll(pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();

        assertPageableHasSorting(pageNumber, pageSize, Sort.Direction.ASC, sortBy, pageable);

        Assertions.assertEquals(2, response.getContent().size());
        Assertions.assertEquals(pageNumber, response.getPageNumber());
        Assertions.assertEquals(pageSize, response.getPageSize());
        Assertions.assertEquals(2, response.getTotalElements());
        Assertions.assertEquals(1 ,response.getTotalPages());
        Assertions.assertTrue(response.isLastPage());
    }

    @Test
    public void getAllProductsTest_ShouldReturnProductResponse_withDescendingSort(){
        int pageNumber = 0;
        int pageSize = 10;
        String sortBy = "price";
        String sortOrder = "desc";

        List<Product> productList = getTestProductList();

        Page<Product> productPage = new PageImpl<>(productList, PageRequest.of(pageNumber, pageSize), 2);

        Mockito.when(productRepository.findAll(Mockito.any(Pageable.class)))
                .thenReturn(productPage);

        ProductResponse response = productService.getAllProducts(pageNumber, pageSize, sortBy, sortOrder);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        Mockito.verify(productRepository).findAll(pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();

        assertPageableHasSorting(pageNumber, pageSize, Sort.Direction.DESC, sortBy, pageable);
        Assertions.assertEquals(2, response.getContent().size());
    }

    @Test
    public void getAllProductsTest_ShouldReturnEmptyResponse_EmptyProductList() {
        int pageNumber = 0;
        int pageSize = 10;
        String sortBy = "price";
        String sortOrder = "desc";

        Page<Product> productPage = new PageImpl<>(List.of(), PageRequest.of(pageNumber, pageSize), 0);

        Mockito.when(productRepository.findAll(Mockito.any(Pageable.class)))
                .thenReturn(productPage);

        ProductResponse response = productService.getAllProducts(pageNumber, pageSize, sortBy, sortOrder);

        Mockito.verify(productRepository).findAll(Mockito.any(Pageable.class));

        Assertions.assertEquals(0, response.getContent().size());
    }

    @Test
    public void getProductsByCategoryTest_ShouldThrowException_CategoryDoesntExist(){
        Long categoryId = 1L;

        Mockito.when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () ->
            productService.getProductsByCategory(categoryId, 0, 10, "productId", "asc"));
    }

    @Test
    public void getProductsByCategoryTest_ShouldReturnProductResponse_CategoryExist(){
        int pageNumber = 0;
        int pageSize = 10;
        String sortBy = "productId";
        String sortOrder = "asc";

        Long categoryId = 1L;
        Category category = getTestCategory(categoryId);

        List<Product> productList = getTestProductList();

        Page<Product> productPage = new PageImpl<>(productList, PageRequest.of(pageNumber, pageSize), 2);

        Mockito.when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));
        Mockito.when(productRepository.findByCategory(Mockito.any(Category.class), Mockito.any(Pageable.class)))
                .thenReturn(productPage);

        ProductResponse response = productService.getProductsByCategory(categoryId, pageNumber, pageSize, sortBy, sortOrder);

        Assertions.assertEquals(2, response.getContent().size());
    }

    @Test
    public void getProductsByCategoryTest_ShouldReturnEmptyProductResponse_NoProductsExist(){
        int pageNumber = 0;
        int pageSize = 10;
        String sortBy = "productId";
        String sortOrder = "asc";

        Long categoryId = 1L;
        Category category = getTestCategory(categoryId);

        Page<Product> productPage = new PageImpl<>(List.of(), PageRequest.of(pageNumber, pageSize), 0);

        Mockito.when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));
        Mockito.when(productRepository.findByCategory(Mockito.any(Category.class), Mockito.any(Pageable.class)))
                .thenReturn(productPage);

        ProductResponse response = productService.getProductsByCategory(categoryId, pageNumber, pageSize, sortBy, sortOrder);

        Mockito.verify(productRepository).findByCategory(Mockito.any(Category.class), Mockito.any(Pageable.class));

        Assertions.assertEquals(0, response.getContent().size());
    }

    @Test
    public void updateProductTest_ShouldReturnProductDTO_ProductExists(){
        Long productId = 1L;
        Product product1 = new Product();
        product1.setProductId(productId);

        ProductDTO inputProductDTO = getTestInputProductDTO();

        Mockito.when(productRepository.findById(productId))
                .thenReturn(Optional.of(product1));

        Mockito.when(productRepository.save(Mockito.any(Product.class)))
                .thenReturn(product1);
        Mockito.when(cartRepository.findCartsByProduct(productId))
                .thenReturn(List.of());

        productService.updateProduct(productId, inputProductDTO);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productRepository).save(productCaptor.capture());
        Product capturedProduct = productCaptor.getValue();

        Double expectedSpecialPrice = inputProductDTO.getPrice() -
                (inputProductDTO.getDiscount() * 0.01 * inputProductDTO.getPrice());

        Assertions.assertEquals(inputProductDTO.getProductName(), capturedProduct.getProductName());
        Assertions.assertEquals(inputProductDTO.getDiscount(), capturedProduct.getDiscount());
        Assertions.assertEquals(inputProductDTO.getDescription(), capturedProduct.getDescription());
        Assertions.assertEquals(inputProductDTO.getQuantity(), capturedProduct.getQuantity());
        Assertions.assertEquals(inputProductDTO.getPrice(), capturedProduct.getPrice());
        Assertions.assertEquals(expectedSpecialPrice, capturedProduct.getSpecialPrice());
    }

    @Test
    public void updateProductTest_ShouldThrowException_ProductDoesntExist(){
        Long productId = 1L;

        ProductDTO inputProductDTO = getTestInputProductDTO();

        Mockito.when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () ->
            productService.updateProduct(productId, inputProductDTO)
        );
    }

    @Test
    public void deleteProductTest_ShouldPass_ProductExist(){
        Long productId = 1L;
        Product product1 = new Product();
        product1.setProductId(productId);

        Mockito.when(productRepository.findById(productId))
                .thenReturn(Optional.of(product1));

        productService.deleteProduct(productId);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productRepository).delete(productCaptor.capture());
        Product capturedProduct = productCaptor.getValue();

        Assertions.assertEquals(productId, capturedProduct.getProductId());
    }

    @Test
    public void deleteProductTest_ShouldThrowException_ProductDoesntExist(){
        Long productId = 1L;

        Mockito.when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                productService.deleteProduct(productId)
        );
    }

    @Test
    public void updateProductImageTest_ShouldPass_ProductExist() throws IOException {
        Long productId = 1L;
        Product product1 = new Product();
        product1.setProductId(productId);
        setImageUploadPath();

        MultipartFile file = Mockito.mock(MultipartFile.class);

        Mockito.when(productRepository.findById(productId))
                .thenReturn(Optional.of(product1));
        Mockito.when(fileService.uploadImage(Mockito.anyString(), Mockito.eq(file)))
                .thenReturn("image.png");
        Mockito.when(productRepository.save(product1))
                .thenReturn(product1);


        ProductDTO updatedProductDTO = productService.updateProductImage(productId, file);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);

        Mockito.verify(productRepository).findById(productId);
        Mockito.verify(fileService).uploadImage(Mockito.any(String.class), Mockito.eq(file));
        Mockito.verify(productRepository).save(productCaptor.capture());

        Product capturedProduct = productCaptor.getValue();

        Assertions.assertEquals("image.png", capturedProduct.getImage());
        Assertions.assertEquals("image.png", updatedProductDTO.getImage());
    }

    @Test
    public void updateProductImageTest_ShouldThrowException_ProductDoesntExist() throws IOException {
        Long productId = 1L;
        MultipartFile file = Mockito.mock(MultipartFile.class);

        Mockito.when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                productService.updateProductImage(productId, file)
        );
        Mockito.verify(fileService, Mockito.never()).uploadImage(Mockito.any(String.class), Mockito.eq(file));
        Mockito.verify(productRepository, Mockito.never()).save(Mockito.any(Product.class));
    }

    @Test
    public void getProductByIdTest_ShouldPass_ProductExist(){
        Long productId = 1L;
        Product product1 = new Product();
        product1.setProductId(productId);

        Mockito.when(productRepository.findById(productId))
                .thenReturn(Optional.of(product1));

        ProductDTO productDTO = productService.getProductById(productId);

        Assertions.assertEquals(productId, productDTO.getProductId());
    }

    @Test
    public void getProductByIdTest_ShouldThrowException_ProductDoesntExist(){
        Long productId = 1L;

        Mockito.when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                productService.getProductById(productId)
        );
    }
}
