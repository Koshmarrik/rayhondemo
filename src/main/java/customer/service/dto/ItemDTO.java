package customer.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by roman rasskazov on 11.06.2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemDTO {

    private String id;

    private String name;

    private CategoryDTO category;

    private String description;

    private List<VariationDTO> variations;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryDTO getCategory() {
        return category;
    }

    public void setCategory(CategoryDTO category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<VariationDTO> getVariations() {
        return variations;
    }

    public void setVariations(List<VariationDTO> variations) {
        this.variations = variations;
    }
}

