package com.example.template_conections.utils;


import java.util.ArrayList;

public class MessageData {
    private ArrayList<String[]> mData;

    public MessageData(String id) {
        mData = new ArrayList<String[]>();
        String[] data = new String[2];
        data[0] = id;
        data[1] = "";
        mData.add(data);
    }

    public MessageData(String id, String value) {
        mData = new ArrayList<String[]>();
        String[] data = new String[2];
        data[0] = id;
        data[1] = value;
        mData.add(data);
    }

    public String getId() {
        return mData.get(0)[0];
    }

    public String getValue() {
        return mData.get(0)[1];
    }

    public String getValue(String id) {
        String value = "";
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i)[0].equalsIgnoreCase(id)) {
                value = mData.get(i)[1];
            }
        }
        return value;
    }

    public void addValue(String id, String value) {
        String[] data = new String[2];
        data[0] = id;
        data[1] = value;
        mData.add(data);
    }

}