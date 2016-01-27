package da.mapper;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;

import java.util.HashMap;
import java.util.Map;

public class MapMapper extends CustomMapper<Map<?,?>, Map<?,?>> {
	public Map map(Map updateMap, Map sourceMap){

		if(updateMap == null){ return sourceMap; }

		if (sourceMap == null){
			sourceMap = new HashMap();
		}

		for (Object entryObj : updateMap.entrySet()){
			Map.Entry<Object, Object> updateEntry = (Map.Entry<Object, Object>) entryObj;

			sourceMap.put(updateEntry.getKey(), updateEntry.getValue());
		}
		return sourceMap;
	}

    @Override
    public void mapAtoB(Map<?,?> a, Map<?,?> b, MappingContext context) {
        map(a,b);
    }

}
