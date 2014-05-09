package model.types;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
public class ResultInfo implements Serializable {

    private List<Runner> runners;

    private List<PhotoFinish> photoFinishes;
}