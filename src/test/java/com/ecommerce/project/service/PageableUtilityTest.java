package com.ecommerce.project.utility;

import com.ecommerce.project.service.impl.PageableUtility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageableUtilityTest {

    @Test
    void getPageableTest_ShouldPass_Ascending(){
        int pageNumber = 0;
        int pageSize = 10;
        String sortBy = "price";
        String sortOrder = "asc";

        Pageable pageable = PageableUtility.getPageable(pageNumber, pageSize, sortBy, sortOrder);

        Assertions.assertEquals(pageNumber, pageable.getPageNumber());
        Assertions.assertEquals(pageSize, pageable.getPageSize());

        Sort.Order order = pageable.getSort().getOrderFor(sortBy);
        Assertions.assertEquals(Sort.Direction.ASC, order.getDirection());
    }

    @Test
    void getPageableTest_ShouldPass_Descending(){
        int pageNumber = 0;
        int pageSize = 10;
        String sortBy = "price";
        String sortOrder = "desc";

        Pageable pageable = PageableUtility.getPageable(pageNumber, pageSize, sortBy, sortOrder);

        Assertions.assertEquals(pageNumber, pageable.getPageNumber());
        Assertions.assertEquals(pageSize, pageable.getPageSize());

        Sort.Order order = pageable.getSort().getOrderFor(sortBy);
        Assertions.assertEquals(Sort.Direction.DESC, order.getDirection());
    }
}
