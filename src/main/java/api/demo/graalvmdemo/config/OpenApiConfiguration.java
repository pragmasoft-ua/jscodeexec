package api.demo.graalvmdemo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Oleksii Drabchak",
                        email = "drabchak.aleksey@gmail.com",
                        url = "https://www.linkedin.com/in/oleksii-drabchak/"
                ),
                description = "A demo project for JS code execution using GraalVM.",
                title = "OpenApi specification - Oleksii Drabchak",
                version = "1.0",
                license = @License(
                        name = "Licence name",
                        url = "https://some-url.com"
                ),
                termsOfService = "Terms of service"
        ),
        servers = {
                @Server(
                        description = "Local ENV",
                        url = "http://localhost:8081/api/v1"
                )
        }
)
public class OpenApiConfiguration {
}
