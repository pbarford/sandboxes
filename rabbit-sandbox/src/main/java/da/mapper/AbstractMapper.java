package da.mapper;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

public abstract class AbstractMapper {

	private final MapperFactory mapperFactory = new DefaultMapperFactory.Builder().mapNulls(false).build();
	private final MapperFacade mapper;

	private final AnnotationBasedReflectiveListMapper listMapper;

	protected AbstractMapper(){
		configureMapper();

		mapper = mapperFactory.getMapperFacade();
		listMapper = new AnnotationBasedReflectiveListMapper();
	}

	protected abstract void configureMapper();

	protected AnnotationBasedReflectiveListMapper getListMapper() {
		return listMapper;
	}

	protected MapperFacade getMapper() {
		return mapper;
	}

	public MapperFactory getMapperFactory() {
		return mapperFactory;
	}
}
