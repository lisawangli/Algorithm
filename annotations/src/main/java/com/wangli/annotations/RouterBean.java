package com.wangli.annotations;

import javax.lang.model.element.Element;

public class RouterBean {

    public enum TypeEnum{
        ACTIVITY,CALL
    }
    private TypeEnum typeEnum;
    private Element element; // 类节点 JavaPoet学习的时候，可以拿到很多的信息
    private Class<?> myClass; // 被注解的 Class对象 例如： MainActivity.class  Main2Activity.class
    private String path;
    private String group;

    public static RouterBean create(TypeEnum typeEnum, Class<?> myClass, String path, String group){
        return new RouterBean(typeEnum, myClass, path, group);
    }
    public RouterBean(TypeEnum typeEnum, Class<?> myClass, String path, String group) {
        this.typeEnum = typeEnum;
        this.myClass = myClass;
        this.path = path;
        this.group = group;
    }

    public TypeEnum getTypeEnum() {
        return typeEnum;
    }

    public void setTypeEnum(TypeEnum typeEnum) {
        this.typeEnum = typeEnum;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public Class<?> getMyClass() {
        return myClass;
    }

    public void setMyClass(Class<?> myClass) {
        this.myClass = myClass;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    private RouterBean(Builder builder){
        this.typeEnum = builder.typeEnum;
        this.element = builder.element;
        this.myClass = builder.myClass;
        this.path = builder.path;
        this.group = builder.group;
    }
    public static class Builder{
        private TypeEnum typeEnum;
        private Element element; // 类节点 JavaPoet学习的时候，可以拿到很多的信息
        private Class<?> myClass; // 被注解的 Class对象 例如： MainActivity.class  Main2Activity.class
        private String path;
        private String group;
        public Builder addType(TypeEnum typeEnum){
            this.typeEnum = typeEnum;
            return this;
        }

        public Builder addElement(Element element){
            this.element = element;
            return this;
        }

        public Builder addClazz(Class<?> myClass){
            this.myClass = myClass;
            return this;
        }

        public Builder addPath(String path){
            this.path = path;
            return this;
        }

        public Builder addGroup(String group){
            this.group = group;
            return this;
        }
        public RouterBean build(){
            if (path ==null || path.length()==0){
                throw new IllegalArgumentException("path必填项为空，如：/app/MainActivity");
            }
            return new RouterBean(this);
        }
    }

    @Override
    public String toString() {
        return "RouterBean{" +
                "typeEnum=" + typeEnum +
                ", element=" + element +
                ", myClass=" + myClass +
                ", path='" + path + '\'' +
                ", group='" + group + '\'' +
                '}';
    }
}
