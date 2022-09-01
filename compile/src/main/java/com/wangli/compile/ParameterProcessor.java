package com.wangli.compile;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.wangli.annotations.Parameter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.wangli.annotations.Parameter"})

public class ParameterProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Types typeUtils;
    private Messager messager;
    private Filer filer;
    //临时map存储，用来存放被parameter注解的属性集合，生成类文件时遍历
    private Map<TypeElement, List<Element>> tempParameterMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
        if (!ProcessorUtils.isEmpty(set)){
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Parameter.class);
            if (ProcessorUtils.isEmpty(elements)){
                return false;
            }
            for (Element element : elements) {//element = age,sex,name
                //字段节点的上一个节点，类节点key 注解在属性上面，属性节点是父节点是类节点
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
                // enclosingElement == Personal_MainActivity == key
                if (tempParameterMap.containsKey(enclosingElement)) {
                    tempParameterMap.get(enclosingElement).add(element);
                }else {
                    List<Element> fields = new ArrayList<>();
                    fields.add(element);
                    tempParameterMap.put(enclosingElement,fields);
                }
            }
            if (ProcessorUtils.isEmpty(tempParameterMap)) return true;
            TypeElement activityType = elementUtils.getTypeElement("android.app.Activity");
            TypeElement parameterType = elementUtils.getTypeElement("com.wangli.arouter_api.ParameterGet");
            ParameterSpec parameterSpec=  ParameterSpec.builder(TypeName.OBJECT,"targetParameter").build();
            for (Map.Entry<TypeElement, List<Element>> entry : tempParameterMap.entrySet()) {
                TypeElement typeElement = entry.getKey();
                if (!typeUtils.isSubtype(typeElement.asType(), activityType.asType())){
                    throw  new RuntimeException("@Parameter注解目前仅限用于activity类之上");
                }
                ClassName className = ClassName.get(typeElement);
                ParameterFactory factory = new ParameterFactory.Builder(parameterSpec).setMessager(messager)
                        .setTypeUtils(typeUtils)
                        .setElements(elementUtils)
                        .setClassName(className).Build();
                factory.addFirstStatement();
                for (Element element : entry.getValue()) {
                    factory.buildStatement(element);
                }
                String finalClassName = typeElement.getSimpleName()+"$$Parameter";
                messager.printMessage(Diagnostic.Kind.NOTE,"APT生成获取参数类文件:"+className.packageName()+"."+finalClassName);
                try {
                    JavaFile.builder(className.packageName(),
                            TypeSpec.classBuilder(finalClassName)
                                    .addSuperinterface(ClassName.get(parameterType))
                                    .addModifiers(Modifier.PUBLIC)
                                    .addMethod(factory.methodBuild())
                                    .build()).build().writeTo(filer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }
}
