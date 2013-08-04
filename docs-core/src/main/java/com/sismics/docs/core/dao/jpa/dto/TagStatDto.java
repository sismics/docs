package com.sismics.docs.core.dao.jpa.dto;


/**
 * Tag DTO.
 *
 * @author bgamard 
 */
public class TagStatDto extends TagDto {

    private int count;

    /**
     * Getter of count.
     *
     * @return the count
     */
    public int getCount() {
        return count;
    }

    /**
     * Setter of count.
     *
     * @param count count
     */
    public void setCount(int count) {
        this.count = count;
    }
}
