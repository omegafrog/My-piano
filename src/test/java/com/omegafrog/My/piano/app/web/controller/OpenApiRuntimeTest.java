package com.omegafrog.My.piano.app.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;

import com.omegafrog.My.piano.app.SwaggerConfig;
import com.omegafrog.My.piano.app.web.controller.AdminCouponController;
import com.omegafrog.My.piano.app.web.service.CouponIssuanceApplicationService;

@SpringBootTest(classes = OpenApiRuntimeTest.OpenApiApplication.class)
@AutoConfigureMockMvc(addFilters = false)
class OpenApiRuntimeTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void exposesCouponContractThroughOpenApiAndSwaggerUi() throws Exception {
        String apiDocs = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());

        assertThat(apiDocs).contains("/api/v1/admin/coupons", "IssueCouponRequest", "CouponResponse");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            SecurityAutoConfiguration.class,
            OAuth2ClientAutoConfiguration.class,
            ManagementWebSecurityAutoConfiguration.class
    })
    @Import({SwaggerConfig.class, AdminCouponController.class})
    static class OpenApiApplication {
        @Bean
        CouponIssuanceApplicationService couponIssuanceApplicationService() {
            return mock(CouponIssuanceApplicationService.class);
        }
    }
}
