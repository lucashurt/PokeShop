package com.example.ecommercefull;

import com.example.ecommercefull.auth.models.Business;
import com.example.ecommercefull.auth.models.RoleEnum;
import com.example.ecommercefull.auth.models.User;
import com.example.ecommercefull.product.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ECommerceJsonTests {

    @Autowired
    private JacksonTester<User> userJSON;
    @Autowired
    private JacksonTester<Product> productJSON;
    @Autowired
    private JacksonTester<List<Product>> productListJSON;

    @Test
    void userSerializationTest() throws IOException {
        User user = new User("lucas", "password", "lucas hurtado", RoleEnum.ROLE_BUSINESS);
        assertThat(userJSON.write(user)).isEqualTo("user.json");
    }

    @Test
    void userDeserializationTest() throws IOException {
        String expectedJson = """
                {
                   "username": "lucas",
                   "password": "password",
                   "fullName": "lucas hurtado",
                   "role": "ROLE_BUSINESS",
                   "cart": {
                       "id": null,
                       "cartItems": []
                   }
                 }
                """;
        assertThat(userJSON.parse(expectedJson).getObject().getUsername()).isEqualTo("lucas");
        assertThat(userJSON.parse(expectedJson).getObject().getPassword()).isEqualTo("password");
        assertThat(userJSON.parse(expectedJson).getObject().getFullName()).isEqualTo("lucas hurtado");
        assertThat(userJSON.parse(expectedJson).getObject().getRole()).isEqualTo(RoleEnum.ROLE_BUSINESS);
        assertThat(userJSON.parse(expectedJson).getObject().getCart().getId()).isNull();
        assertThat(userJSON.parse(expectedJson).getObject().getCart().getCartItems().isEmpty()).isTrue();
    }

    @Test
    void productSerializationTest() throws IOException {
        Business business = new Business("business","password","business account",RoleEnum.ROLE_BUSINESS);
        Product product = new Product("product","product description",19.99,5,business);
        assertThat(productJSON.write(product)).isEqualTo("product.json");
    }

    @Test
    void productDeserializationTest() throws IOException {
        String expectedJson = """
                {
                  "id": null,
                  "name": "product",
                  "description": "product description",
                  "price": 19.99,
                  "stock": 5,
                  "business": {
                    "id": null,
                    "username": "business"
                  }
                }
                """;
        assertThat(productJSON.parse(expectedJson).getObject().getName()).isEqualTo("product");
        assertThat(productJSON.parse(expectedJson).getObject().getDescription()).isEqualTo("product description");
        assertThat(productJSON.parse(expectedJson).getObject().getPrice()).isEqualTo(19.99);
        assertThat(productJSON.parse(expectedJson).getObject().getStock()).isEqualTo(5);
        assertThat(productJSON.parse(expectedJson).getObject().getBusiness().getUsername()).isEqualTo("business");
    }


    @Test
    void productListSerializationTest() throws IOException {
        Business business = new Business("business","password","business account",RoleEnum.ROLE_BUSINESS);
        Product product1 = new Product("product","product description",19.99,5,business);
        Product product2 = new Product("product2","another product description",19.99,5,business);
        List<Product> products = new ArrayList<>();
        products.add(product1);
        products.add(product2);
        assertThat(productListJSON.write(products)).isEqualToJson("productList.json");
    }

    @Test
    void productListDeserializationTest() throws IOException {
        String expectedJson = """
                                    [
                                       {
                                         "id": null,
                                         "name": "product",
                                         "description": "product description",
                                         "price": 19.99,
                                         "stock": 5,
                                         "business": {
                                           "id": null,
                                           "username": "business"
                                         }
                                       },
                                       {
                                         "id": null,
                                         "name": "product2",
                                         "description": "another product description",
                                         "price": 19.99,
                                         "stock": 5,
                                         "business": {
                                           "id": null,
                                           "username": "business"
                                         }
                                       }
                                     ]
                                 """;
        Product[] parsedProducts = productListJSON.parse(expectedJson).getObject().toArray(new Product[0]);
        assertThat(parsedProducts.length).isEqualTo(2);

        assertThat(parsedProducts[0].getName()).isEqualTo("product");
        assertThat(parsedProducts[0].getDescription()).isEqualTo("product description");
        assertThat(parsedProducts[0].getPrice()).isEqualTo(19.99);
        assertThat(parsedProducts[0].getStock()).isEqualTo(5);
        assertThat(parsedProducts[0].getBusiness().getUsername()).isEqualTo("business");

        assertThat(parsedProducts[1].getName()).isEqualTo("product2");
        assertThat(parsedProducts[1].getDescription()).isEqualTo("another product description");
        assertThat(parsedProducts[1].getPrice()).isEqualTo(19.99);
        assertThat(parsedProducts[1].getStock()).isEqualTo(5);
        assertThat(parsedProducts[1].getBusiness().getUsername()).isEqualTo("business");
    }
}
