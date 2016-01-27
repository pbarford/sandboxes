package da.mapper;


import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import model.validation.custom.annotation.MappingKey;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class AnnotationBasedReflectiveListMapper {

    private final Map<Class, Field> mappingKeyFieldMap = new ConcurrentHashMap<Class, Field>();

	public List map(List updateList, List destinationList, MapperFacade mapper){
		if(isNull(updateList)){
            return destinationList;
        }
		if(isNull(destinationList)){
			destinationList = new ArrayList();
		}
		for (Object updateObject : updateList){
            if(!hasAnnotatedField(updateObject)) {
                return handleListWithoutMappingKey(updateList, destinationList);
            }
            mergeObjectIntoExistingList(updateObject, destinationList, mapper);
		}
		return destinationList;
	}

    private void mergeObjectIntoExistingList(Object updateObject, List destinationList, MapperFacade mapper) {
        Object keyFieldValue = getKeyByAnnotation(updateObject);

		if(keyFieldValue == null){
			log.error("Cannot map the following due to either that the annotation is not available or annotated keyFieldValue is null on " + updateObject);
			return;
		}

        Object destinationObject = findObjectWithMatchingKeyValue(destinationList, keyFieldValue);

        if (exists(destinationObject)){
            mapper.map(updateObject, destinationObject);
        } else {
            destinationList.add(updateObject);
        }
    }

    private List handleListWithoutMappingKey(List updateList, List destinationList) {
        destinationList.clear();
        destinationList.addAll(updateList);
        return updateList;
    }

    private Object findObjectWithMatchingKeyValue(List objectList, Object lookupKey) {
		for (Object object : objectList){
			Object objectKey = getKeyByAnnotation(object);
			if (lookupKey.equals(objectKey)){
                return object;
            }
		}
		return null;
	}

	private Object getKeyByAnnotation(Object object) {
		Field keyField = findAnnotatedField(object);
        if (isNull(keyField)){
            return null;
        }
		if(!keyField.isAccessible()){
			keyField.setAccessible(true);
		}
		return getFieldAnnotatedWithKey(object, keyField);
	}

	private Field findAnnotatedField(Object object) {
        Field cachedField = mappingKeyFieldMap.get(object.getClass());
        if (cachedField!=null){
            return cachedField;
        }
		for (Field field : object.getClass().getDeclaredFields()){

			MappingKey mappingKey = field.getAnnotation(MappingKey.class);
			if (exists(mappingKey)){
                mappingKeyFieldMap.put(object.getClass(),field);
				return field;
			}
		}
		return null;
	}

	private Object getFieldAnnotatedWithKey(Object object, Field field) {
		Object key;
		try {
			key = field.get(object);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Unable to access Key field");
		}
		return key;
	}

    private boolean hasAnnotatedField(Object object) {
        return findAnnotatedField(object) != null;
    }

    private boolean isNull(Object object) {
        return object == null;
    }

    private boolean exists(Object object) {
        return object != null;
    }
}
