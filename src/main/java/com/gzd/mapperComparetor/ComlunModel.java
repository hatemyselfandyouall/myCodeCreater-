package com.gzd.mapperComparetor;

/**
 * Created by YS-GZD-1495 on 2017/6/9.
 */
public class ComlunModel implements  Comparable{
    private String name;
    private String type;
    private String size;
    private String ableNull;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getAbleNull() {
        return ableNull;
    }

    public void setAbleNull(String ableNull) {
        this.ableNull = ableNull;
    }

    @Override
    public int compareTo(Object o) {
        if(o instanceof ComlunModel){
            return compareTo((ComlunModel)o);
        }
        return 0;
    }
    public int compareTo(ComlunModel o) {
        return this.getName().compareTo(o.getName());
    }
    public boolean equals(Object o){
        if(o instanceof ComlunModel){
            return equals((ComlunModel)o);
        }
        return true;
    }
    public boolean equals(ComlunModel o){
        return this.getName().equals(o.getName());
    }
}
