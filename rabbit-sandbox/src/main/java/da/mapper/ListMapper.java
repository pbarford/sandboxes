package da.mapper;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;

import java.util.List;

public class ListMapper extends CustomMapper<List<?>, List<?>> {

    private AnnotationBasedReflectiveListMapper annotationBasedReflectiveListMapper = new AnnotationBasedReflectiveListMapper();

    @Override
    public void mapAtoB(List<?> a, List<?> b, MappingContext context) {
         annotationBasedReflectiveListMapper.map(a,b,mapperFacade);
    }
}
