package reciter.dynamicfilter;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.util.UriComponentsBuilder;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.json.JsonMapper;

@ControllerAdvice
public class GlobalDynamicFilterAdvice implements ResponseBodyAdvice<Object> {

	private final JsonMapper jsonMapper;
	 
    public GlobalDynamicFilterAdvice(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }
 
    @Override
    public boolean supports(
            MethodParameter returnType,
            Class<? extends HttpMessageConverter<?>> converterType) {
 
        return JacksonJsonHttpMessageConverter.class
                .isAssignableFrom(converterType);
    }
 
    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType contentType,
            Class<? extends HttpMessageConverter<?>> converterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
 
        String fieldsParam = UriComponentsBuilder
                .fromUri(request.getURI())
                .build()
                .getQueryParams()
                .getFirst("fields");
 
        if (fieldsParam == null || fieldsParam.isBlank() || body == null) {
            return body;
        }
 
        Set<String> fields = Arrays.stream(fieldsParam.split(","))
                .map(String::trim)
                .filter(field -> !field.isEmpty())
                .collect(Collectors.toCollection(HashSet::new));
 
        Map<String, Object> fieldTree = FieldParser.parse(fields);

        try {
            ObjectWriter writer = new DynamicFilterProvider(fieldTree).getWriter(jsonMapper);
            return jsonMapper.readTree(writer.writeValueAsString(body));
        } catch (JacksonException e) {
            throw new IllegalStateException("Dynamic filtering failed", e);
        }
    }
}
