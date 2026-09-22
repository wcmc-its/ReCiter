package reciter.dynamicfilter;



import java.util.Map;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.PropertyFilter;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;
import tools.jackson.databind.ser.std.SimpleFilterProvider;

public class DynamicFilterProvider {

    private final Map<String, Object> fields;

    public DynamicFilterProvider(Map<String, Object> fields) {
        this.fields = fields;
    }

    public ObjectWriter getWriter(ObjectMapper mapper) {
        PropertyFilter filter = new SimpleBeanPropertyFilter() {
            @Override
            protected boolean include(BeanPropertyWriter writer) {
                return fields == null || fields.containsKey(writer.getName());
            }
        };

        SimpleFilterProvider provider = new SimpleFilterProvider();
        provider.addFilter("dynamicFilter", filter);
        provider.setFailOnUnknownId(false); // models without @JsonFilter

        return mapper.writer(provider);
    }
}


