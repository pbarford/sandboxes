package da.mapper;

import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFactory;
import da.model.Event;
import da.model.Market;
import da.model.Selection;

@Slf4j
public class EventMapper extends AbstractMapper{

	@Override
	protected void configureMapper() {
        MapperFactory mapperFactory = getMapperFactory();
        mapperFactory.classMap(Event.class, Event.class).exclude("id").byDefault().register();
        mapperFactory.classMap(Market.class, Market.class).exclude("id").byDefault().register();
        mapperFactory.classMap(Selection.class, Selection.class).exclude("id").byDefault().register();
        mapperFactory.registerMapper(new ListMapper());
        mapperFactory.registerMapper(new MapMapper());
	}

	public void overlayEventChanges(Event destination, Event source) {
		getMapper().map(source, destination);
    }


}
