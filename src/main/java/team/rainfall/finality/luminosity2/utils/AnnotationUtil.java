package team.rainfall.finality.luminosity2.utils;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import team.rainfall.finality.FinalityLogger;

@SuppressWarnings("unused")
public class AnnotationUtil {
    public static boolean annotationExists(String name, MethodNode node) {
        if (node.visibleAnnotations != null) {
            for (AnnotationNode annotation : node.visibleAnnotations) {
                FinalityLogger.debug("DBG2 " + annotation.desc);
                if (annotation.desc.equals(name)) {
                    return true;
                }
            }
        }
        if (node.invisibleAnnotations != null) {
            for (AnnotationNode annotation : node.invisibleAnnotations) {
                FinalityLogger.debug("DBG2 " + annotation.desc);
                if (annotation.desc.equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean annotationExists(String name, ClassNode node) {
        //FinalityLogger.info("START");
        if (node.visibleAnnotations == null) return false;
        //FinalityLogger.info("START2");
        for (AnnotationNode annotation : node.visibleAnnotations) {
            //FinalityLogger.info("START3 "+annotation.desc);
            if (annotation.desc.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static AnnotationNode getAnnotation(String desc, MethodNode node) {
        if (node.visibleAnnotations != null) {
            for (AnnotationNode annotation : node.visibleAnnotations) {
                if (annotation.desc.equals(desc)) {
                    return annotation;
                }
            }
        }
        if (node.invisibleAnnotations != null) {
            for (AnnotationNode annotation : node.invisibleAnnotations) {
                if (annotation.desc.equals(desc)) {
                    return annotation;
                }
            }
        }
        return null;
    }

    public static AnnotationNode getAnnotation(String desc, ClassNode node) {
        if (node.visibleAnnotations != null) {
            for (AnnotationNode annotation : node.visibleAnnotations) {
                if (annotation.desc.equals(desc)) {
                    return annotation;
                }
            }
        }
        if (node.invisibleAnnotations != null) {
            for (AnnotationNode annotation : node.invisibleAnnotations) {
                if (annotation.desc.equals(desc)) {
                    return annotation;
                }
            }
        }
        return null;
    }

    public static boolean annotationExists(Class<?> clazz, MethodNode node) {
        return annotationExists(desc(clazz), node);
    }

    public static boolean annotationExists(Class<?> clazz, ClassNode node) {
        return annotationExists(desc(clazz), node);
    }

    public static AnnotationNode getAnnotation(Class<?> clazz, MethodNode node) {
        return getAnnotation(desc(clazz), node);
    }

    public static AnnotationNode getAnnotation(Class<?> clazz, ClassNode node) {
        return getAnnotation(desc(clazz), node);
    }

    private static String desc(Class<?> clazz) {
        return "L" + clazz.getName().replace('.', '/') + ";";
    }

    public static Object getAnnotationValue(String key, AnnotationNode node) {
        boolean foundTarget = false;
        boolean isKey = true;
        for (Object obj : node.values) {
            if (obj.toString().equals(key) && isKey) {
                foundTarget = true;
                continue;
            }
            if (foundTarget) {
                return obj;
            }
            isKey = !isKey;
        }
        return null;
    }
}
