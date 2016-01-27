package test;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.node.Node;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

public class UpdateMapping {

    private static final String INDEX_NAME = "dasearch";
    private static final String MAPPING = "{\"**\":{\"properties\":{\"action\":{\"type\":\"string\"},\"bettingStatus\":{\"type\":\"string\"},\"clockStage\":{\"properties\":{\"currentPeriodStartTime\":{\"type\":\"long\"},\"periodId\":{\"type\":\"long\"}}},\"display\":{\"type\":\"boolean\"},\"displayOrder\":{\"type\":\"long\"},\"eventDescriptor\":{\"properties\":{\"displayTypeId\":{\"type\":\"long\"},\"displayTypeName\":{\"type\":\"string\"},\"subclassId\":{\"type\":\"long\"},\"subclassName\":{\"type\":\"string\"},\"typeId\":{\"type\":\"long\"},\"typeName\":{\"type\":\"string\"}}},\"extraEventInformation\":{\"type\":\"string\"},\"hasOfferedInRunningChanged\":{\"type\":\"boolean\"},\"id\":{\"type\":\"long\"},\"live\":{\"type\":\"string\"},\"markets\":{\"type\":\"nested\",\"properties\":{\"action\":{\"type\":\"string\"},\"bettingStatus\":{\"type\":\"string\"},\"display\":{\"type\":\"boolean\"},\"displayOrder\":{\"type\":\"long\"},\"handicapValue\":{\"type\":\"double\"},\"id\":{\"type\":\"long\"},\"inRunning\":{\"type\":\"boolean\"},\"indexResults\":{\"properties\":{\"indexValue\":{\"type\":\"long\"},\"resulted\":{\"type\":\"boolean\"}}},\"indexValue\":{\"type\":\"long\"},\"maxAccumulator\":{\"type\":\"long\"},\"minAccumulator\":{\"type\":\"long\"},\"name\":{\"type\":\"string\"},\"offeredInRunning\":{\"type\":\"boolean\"},\"resulted\":{\"type\":\"boolean\"},\"selections\":{\"type\":\"nested\",\"properties\":{\"action\":{\"type\":\"string\"},\"alternativeName\":{\"type\":\"string\"},\"bettingStatus\":{\"type\":\"string\"},\"characteristic\":{\"type\":\"string\"},\"display\":{\"type\":\"boolean\"},\"displayOrder\":{\"type\":\"long\"},\"id\":{\"type\":\"long\"},\"indexResults\":{\"properties\":{\"indexValue\":{\"type\":\"long\"},\"resultType\":{\"type\":\"string\"}}},\"name\":{\"type\":\"string\"},\"previousPriceDenominator\":{\"type\":\"long\"},\"previousPriceNumerator\":{\"type\":\"long\"},\"priceDecimal\":{\"type\":\"string\"},\"priceDenominator\":{\"type\":\"long\"},\"priceNumerator\":{\"type\":\"long\"},\"resultType\":{\"type\":\"string\"},\"typeId\":{\"type\":\"long\"}}},\"sort\":{\"type\":\"string\"},\"typeId\":{\"type\":\"long\"}}},\"matchDetails\":{\"properties\":{\"participantAMatchDetails\":{\"properties\":{\"matchDetailType\":{\"type\":\"string\"},\"participantName\":{\"type\":\"string\"},\"period\":{\"type\":\"string\"},\"time\":{\"type\":\"long\"}}}}},\"matchStatistics\":{\"properties\":{\"participantA\":{\"type\":\"long\"},\"participantB\":{\"type\":\"long\"},\"type\":{\"type\":\"string\"}}},\"name\":{\"type\":\"string\"},\"noMoreBetsTime\":{\"type\":\"long\"},\"offeredInRunning\":{\"type\":\"boolean\"},\"participantAScore\":{\"type\":\"string\"},\"participantBScore\":{\"type\":\"string\"},\"prematchId\":{\"type\":\"long\"},\"prematchOnly\":{\"type\":\"boolean\"},\"properties\":{\"properties\":{\"GAME_LENGTH\":{\"type\":\"string\"}}},\"scheduling\":{\"properties\":{\"scheduledStartTime\":{\"type\":\"long\"}}},\"scoreDetails\":{\"properties\":{\"participantAScores\":{\"properties\":{\"participantName\":{\"type\":\"string\"},\"period\":{\"type\":\"string\"},\"timeOfScore\":{\"type\":\"long\"}}},\"participantBScores\":{\"properties\":{\"participantName\":{\"type\":\"string\"},\"period\":{\"type\":\"string\"},\"timeOfScore\":{\"type\":\"long\"}}}}}}}}";

    public static void main(String[] args) {

        Node node = nodeBuilder().node();
        //CreateIndexResponse res = node.client().admin().indices().create(indexFor("nike")).actionGet();
        //System.out.println(res.isAcknowledged());

        //PutMappingResponse res = node.client().admin().indices().preparePutMapping(INDEX_NAME).setType("nike").setSource(getMappingFor("nike")).execute().actionGet();
        //System.out.println(res.isAcknowledged());
        //res = node.client().admin().indices().preparePutMapping(INDEX_NAME).setType("pp_openbet").setSource(getMappingFor("pp_openbet")).execute().actionGet();
        //System.out.println(res.isAcknowledged());
        //res = node.client().admin().indices().preparePutMapping(INDEX_NAME).setType("sb_openbet").setSource(getMappingFor("sb_openbet")).execute().actionGet();
        //System.out.println(res.isAcknowledged());

        PutMappingResponse res = node.client().admin().indices().preparePutMapping(INDEX_NAME).setType("test").setSource(getMappingFor("test")).execute().actionGet();
        System.out.println(res.isAcknowledged());

        /*
        GetResponse res2 = node.client()
                .prepareGet("dasearch", "facts", "1719826")
                .execute()
                .actionGet();

        System.out.println(new String(res2.getSourceAsBytes(), Charset.defaultCharset()));
        */
        node.close();
    }

    private static CreateIndexRequest indexFor(String type) {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(INDEX_NAME);
        createIndexRequest.mapping(type, MAPPING);
        return createIndexRequest;
    }

    private static String getMappingFor(String type) {
        return MAPPING.replace("**", type);
    }

}
