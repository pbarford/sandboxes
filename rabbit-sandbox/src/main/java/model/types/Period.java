package model.types;

public enum Period {
   
    PRE_MATCH,       
    PERIOD_1,
    END_PERIOD_1,    
    PERIOD_2,
    END_PERIOD_2,    
    PERIOD_3,
    END_PERIOD_3,
    PERIOD_4,
    END_PERIOD_4,
    OVERTIME,
    END_OVERTIME,
    EXTRA_TIME_1,
    END_EXTRA_TIME_1,
    EXTRA_TIME_2,
    END_EXTRA_TIME_2,
    PENALTIES,
    END,
    NOT_APPLICABLE,
    
    // These periods should be removed asap
    @Deprecated H1, 
    @Deprecated H2, 
    @Deprecated HT,
    @Deprecated ETH1, 
    @Deprecated ETH2,
    @Deprecated ETH; 

}