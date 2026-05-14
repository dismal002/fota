package com.foss.fota.utils;

import java.util.List;

public class RootErrJson {
    private List<String> add;
    private List<String> delete;
    private List<String> modify;

    public List<String> getAdd() {
        return this.add;
    }

    public void setAdd(List<String> list) {
        this.add = list;
    }

    public List<String> getModify() {
        return this.modify;
    }

    public void setModify(List<String> list) {
        this.modify = list;
    }

    public List<String> getDelete() {
        return this.delete;
    }

    public void setDelete(List<String> list) {
        this.delete = list;
    }
}
