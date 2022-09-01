package com.wangli.compile;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import com.wangli.annotations.ARouter;
import com.wangli.annotations.RouterBean;

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
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * $T表示类，接口，枚举类型
 * $S表示字符串
 * $N用于引用另一个使用javaPoet生成的方法或者变量
 * $L表示数字类型
 */
@AutoService(Processor.class)
// 允许/支持的注解类型，让注解处理器处理
@SupportedAnnotationTypes({"com.wangli.annotations.ARouter"})

// 指定JDK编译版本
@SupportedSourceVersion(SourceVersion.RELEASE_7)

// 注解处理器接收的参数
@SupportedOptions({"packageNameForAPT", "moduleName"})
public class AnnotationCompile extends AbstractProcessor {
    // 操作Element的工具类（类，函数，属性，其实都是Element）
    private Elements elementTool;

    // type(类信息)的工具类，包含用于操作TypeMirror的工具方法
    private Types typeTool;

    // Message用来打印 日志相关信息
    private Messager messager;

    // 文件生成器， 类 资源 等，就是最终要生成的文件 是需要Filer来完成的
    private Filer filer;

    private String options; // 各个模块传递过来的模块名 例如：app order personal
    private String aptPackage; // 各个模块传递过来的目录 用于统一存放 apt生成的文件


    // 仓库二 Group 缓存二
    // Map<"personal", "ARouter$$Path$$personal.class">
    private Map<String, String> mAllGroupMap = new HashMap<>();
    private Map<String, List<RouterBean>> allPathMao = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);;
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        typeTool = processingEnvironment.getTypeUtils();
        elementTool = processingEnvironment.getElementUtils();

        // 只有接受到 App壳 传递过来的书籍，才能证明我们的 APT环境搭建完成
        options = processingEnvironment.getOptions().get("moduleName");
        aptPackage = processingEnvironment.getOptions().get("packageNameForAPT");
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> options:" + options);
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> aptPackage:" + aptPackage);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty())
            return false;
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ARouter.class);
       //通过type获取activity
        TypeElement activityType = elementTool.getTypeElement("android.app.Activity");
       //显示类信息
        TypeMirror activityTypeMirror = activityType.asType();;
        for (Element element : elements) {
//            String packname=  elementTool.getPackageOf(element).getQualifiedName().toString();
//            String className = element.getSimpleName().toString();
//            String finalClassName = className+"$$ARouter";
//            messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> finalClassName:" + finalClassName);
//
//            ARouter aRouter = element.getAnnotation(ARouter.class);
//            MethodSpec methodSpec = MethodSpec.methodBuilder("findTargetClass")
//                    .addModifiers(Modifier.PUBLIC,Modifier.STATIC)
//                    .returns(Class.class)
//                    .addParameter(String.class,"path")
//                    .addStatement("return path.equals($S)?$T.class:null",aRouter.path(),
//                            ClassName.get((TypeElement)element))
//                    .build();
//            TypeSpec myclass = TypeSpec.classBuilder(finalClassName)
//                    .addModifiers(Modifier.PUBLIC)
//                    .addMethod(methodSpec)
//                    .build();
//            JavaFile javaFile = JavaFile.builder(packname,myclass).build();
//            try {
//                javaFile.writeTo(filer);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            String className = element.getSimpleName().toString();
            ARouter aRouter = element.getAnnotation(ARouter.class);
            messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> className:" + className);

            RouterBean routerBean = new RouterBean.Builder()
                    .addGroup(aRouter.group())
                    .addPath(aRouter.path())
                    .addElement(element)
                    .build();
            TypeMirror elementMiror = element.asType();
            messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> elementMiror:" + elementMiror);
            if (typeTool.isSubtype(elementMiror, activityTypeMirror)) {
                routerBean.setTypeEnum(RouterBean.TypeEnum.ACTIVITY);
            } else {
                throw new IllegalArgumentException("该注解仅仅用于activity之上");
            }
            if (checkRouterPath(routerBean)) {
                messager.printMessage(Diagnostic.Kind.NOTE, "RouterBean Check Success:" + routerBean.toString());

                List<RouterBean> routerBeans = allPathMao.get(routerBean.getGroup());
                if (ProcessorUtils.isEmpty(routerBeans)) {
                    routerBeans = new ArrayList<>();
                    routerBeans.add(routerBean);
                    allPathMao.put(routerBean.getGroup(), routerBeans);
                } else {
                    routerBeans.add(routerBean);
                }
            } else { // ERROR 编译期发生异常
                messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置，如：/app/MainActivity");
            }
        }
            TypeElement pathType = elementTool.getTypeElement("com.wangli.arouter_api.ARouterPath"); // ARouterPath描述
            TypeElement groupType = elementTool.getTypeElement("com.wangli.arouter_api.ARouterGroup");

        messager.printMessage(Diagnostic.Kind.NOTE, groupType+">>>>>>>>>>>>>>>>>>>>>> pathType:" + pathType);
            try {
                createPathFile(pathType);

                createGroupFile(groupType,pathType);

            } catch (IOException e) {
                e.printStackTrace();
                messager.printMessage(Diagnostic.Kind.NOTE, "在生成GROUP模板时，异常了 e:" + e.getMessage());

            }

        return true;
    }

    private void createPathFile(TypeElement pathType) throws IOException {
        // Map<String, RouterBean>
        TypeName typeName = ParameterizedTypeName.get(ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(RouterBean.class));
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>createPathFile>>>>>>>>>>>>");

        for (Map.Entry<String, List<RouterBean>> stringListEntry : allPathMao.entrySet()) {
            messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>stringListEntry>>>>>>>>>>>>"+stringListEntry.getKey());

            MethodSpec.Builder method = MethodSpec.methodBuilder("getPathMap")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(typeName);
            messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>1111>>>>>>>>>>>>");

            // Map<String, RouterBean> pathMap = new HashMap<>(); // $N == 变量 为什么是这个，因为变量有引用 所以是$N
            method.addStatement("$T<$T,$T> $N = new $T<>()"
                    ,ClassName.get(Map.class),
                    ClassName.get(String.class)
            ,ClassName.get(RouterBean.class)
            ,"pathMap"
            ,ClassName.get(HashMap.class));


            List<RouterBean> value = stringListEntry.getValue();
            for (RouterBean routerBean : value) {
                //        pathMap.put("/personal/Personal_MainActivity",
                //                RouterBean.create(RouterBean.TypeEnum.ACTIVITY,
                //                                  Order_MainActivity.class,
                //                           "/personal/Personal_MainActivity",
                //                          "personal"));

                method.addStatement("pathMap.put($S,$T.create($T.$L,$T.class,$S,$S))",
                        routerBean.getPath(),
                        ClassName.get(RouterBean.class),
                        ClassName.get(RouterBean.TypeEnum.class),
                        routerBean.getTypeEnum(),
                        ClassName.get((TypeElement) routerBean.getElement()), // MainActivity.class Main2Activity.class
                        routerBean.getPath(),
                        routerBean.getGroup()
                );

            }

            method.addStatement("return $N","pathMap");
            messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>6666>>>>>>>>>>>>");

            String finalClassName = "ARouter$$Path$$" + stringListEntry.getKey();
            JavaFile.builder(aptPackage,
                    TypeSpec.classBuilder(finalClassName)
                            .addSuperinterface(ClassName.get(pathType))
                            .addModifiers(Modifier.PUBLIC)
                            .addMethod(method.build())
                            .build()
            ).build().writeTo(filer);
            messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>finalClassName>>>>>>>>>>>>"+method.build().toString());

            mAllGroupMap.put(stringListEntry.getKey(),finalClassName);

        }
    }

    private void createGroupFile(TypeElement groupType,TypeElement pathType) throws IOException {
        if (ProcessorUtils.isEmpty(mAllGroupMap) || ProcessorUtils.isEmpty(allPathMao)) return;
        // 返回值 这一段 Map<String, Class<? extends ARouterPath>>

        TypeName typeName = ParameterizedTypeName.get(ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class)
                , WildcardTypeName.subtypeOf(ClassName.get(pathType))));//<? extends ARouterPath>

        // 1.方法 public Map<String, Class<? extends ARouterPath>> getGroupMap() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getGroupMap")
                .addAnnotation(Override.class)
                .returns(typeName)
                .addModifiers(Modifier.PUBLIC);
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>*****>>>>>>>>>>>>");

        // Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
        builder.addStatement("$T<$T,$T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class)
                , WildcardTypeName.subtypeOf(ClassName.get(pathType))),
                "groupMap",
                ClassName.get(HashMap.class));
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>$$$$$$>>>>>>>>>>>>");

        //  groupMap.put("personal", ARouter$$Path$$personal.class);

        for (Map.Entry<String, String> stringStringEntry : mAllGroupMap.entrySet()) {
            builder.addStatement("groupMap.put($S,$T.class)",stringStringEntry.getKey(),
                    ClassName.get(aptPackage,stringStringEntry.getValue()));
        }
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>6666>>>>>>>>>>>>");

        builder.addStatement("return $N","groupMap");

        String finalClassName = "ARouter$$Group$$" + options;
        JavaFile.builder(aptPackage,
                TypeSpec.classBuilder(finalClassName)
                .addSuperinterface(ClassName.get(groupType))
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(builder.build())
                .build()).build().writeTo(filer);
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>=====>>>>>>>>>>>>");


    }

    private final boolean checkRouterPath(RouterBean routerBean) {
        String group = routerBean.getGroup();
        String path = routerBean.getPath();
        if (ProcessorUtils.isEmpty(path)||!path.startsWith("/")){
            return false;
        }
        if (path.lastIndexOf("/") == 0) {
            // 架构师定义规范，让开发者遵循
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置，如：/app/MainActivity");
            return false;
        }
        String finalGroup = path.substring(1, path.indexOf("/", 1));
        if (!ProcessorUtils.isEmpty(group) && !group.equals(options)) {
            // 架构师定义规范，让开发者遵循
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的group值必须和子模块名一致！");
            return false;
        } else {
            routerBean.setGroup(finalGroup);
        }
        return true;
    }
}