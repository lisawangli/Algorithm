package com.wangli.compile;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.wangli.annotations.Parameter;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public class ParameterFactory {
    //方法的构建
    private MethodSpec.Builder method;
    //类名
    private ClassName className;
    private Messager messager;
    //类信息工具类
    private Types typeUtils;
    //获取元素接口信息
    private TypeMirror callMirror;

    private ParameterFactory(Builder builder ){
        this.messager = builder.messager;
        this.className = builder.className;
        this.typeUtils = builder.typeUtils;
        method = MethodSpec.methodBuilder("getParameter")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(builder.parameterSpec);
        this.callMirror = builder.elementUtils.
                getTypeElement("com.wangli.arouter_api.Call")
                .asType();
    }
    /** 只有一行
     * Personal_MainActivity t = (Personal_MainActivity) targetParameter;
     */
    public void addFirstStatement() {
        method.addStatement("$T t = ($T)"+"targetParameter",className,className);
    }

    /**
     *     * 构建方体内容，如：t.s = t.getIntent.getStringExtra("s");
     * @return
     */
    public void buildStatement(Element element) {
        //遍历注解，生成函数体
        TypeMirror typeMirror = element.asType();
        //获取typeKind枚举类型序列号
        int type = typeMirror.getKind().ordinal();
        //获取属性的名字
        String  fieldName = element.getSimpleName().toString();
        //获取注解的值
        String annotationValue = element.getAnnotation(Parameter.class).name();
        annotationValue = ProcessorUtils.isEmpty(annotationValue)?fieldName:annotationValue;
        // t.s = t.getIntent().

        String finalvalue = "t."+fieldName;
        // TODO t.name = t.getIntent().getStringExtra("name");
        String methodContent = finalvalue+"= t.getIntent().";
        if (type == TypeKind.INT.ordinal()){
            methodContent +="getIntExtra($S,"+finalvalue+")";
        } else if(type==TypeKind.BOOLEAN.ordinal()){
            methodContent +="getBooleanExtra($S,"+finalvalue+")";
        } else {
            if (typeMirror.toString().equalsIgnoreCase("java.lang.String")){
                methodContent = "getStringExtra($S)";
            } else if(typeUtils.isSubtype(typeMirror,callMirror)) {
                // t.orderDrawable = (OrderDrawable) RouterManager.getInstance().build("/order/getDrawable").navigation(t);

                methodContent = "t."+fieldName+"=($T)$T.getInstance().build($S).navigation(t)";
                method.addStatement(methodContent, TypeName.get(typeMirror),
                        ClassName.get("com.wangli.arouter_api","RouterManager"),
                        annotationValue);
                return;
            } else {
                methodContent = "t.getIntent().getSerializableExtra($S)";
            }
        }

        if(methodContent.contains("Serializable")) {
            // t.student=(Student) t.getIntent().getSerializableExtra("student");  同学们注意：为了强转
            method.addStatement(finalvalue+"=($T)"+methodContent,ClassName.get(element.asType()),annotationValue);
        } else if (methodContent.endsWith(")")){
            // t.age = t.getIntent().getBooleanExtra("age", t.age ==  9);
            method.addStatement(methodContent,annotationValue);
        } else{
            messager.printMessage(Diagnostic.Kind.ERROR, "目前暂支持String、int、boolean传参");

        }
    }

    public MethodSpec methodBuild() {
        return method.build();
    }

    public static class Builder{
        private ClassName className;
        private Messager messager;
        private Elements elementUtils;
        private Types typeUtils;
        private ParameterSpec parameterSpec;

        public Builder setMessager(Messager messager){
            this.messager = messager;
            return this;
        }
        public Builder setElements(Elements elements){
            this.elementUtils = elements;
            return this;
        }

        public Builder(ParameterSpec parameterSpec){
            this.parameterSpec =parameterSpec;
        }
        public Builder setTypeUtils(Types typeUtils) {
            this.typeUtils = typeUtils;
            return this;
        }

        public Builder setClassName(ClassName className){
            this.className = className;
            return this;
        }

        public ParameterFactory Build(){
            if (parameterSpec== null ){
                throw new NullPointerException("parameterSpec不能为空");
            }
            if (className==null){
                throw new NullPointerException("className不能为空");
            }
            if (messager==null){
                throw new NullPointerException("messager不能为空");
            }
            return new ParameterFactory(this);
        }
    }

}
