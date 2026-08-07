package com.mark.community.enums;

public enum PostCategory {
    질문, 토론, 정보공유, 스터디모집, 공지;

    public static PostCategory checkCategory(String category){

        for(PostCategory postcategory : values()){
            if(postcategory.name().equals(category)) return postcategory;
        }
        throw new IllegalArgumentException("존재하지 않는 카테고리입니다: " + category);
    }
}
