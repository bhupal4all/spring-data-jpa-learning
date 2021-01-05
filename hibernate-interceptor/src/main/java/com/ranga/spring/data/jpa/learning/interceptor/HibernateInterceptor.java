package com.ranga.spring.data.jpa.learning.interceptor;

import com.ranga.spring.data.jpa.learning.SpringContextUtil;
import com.ranga.spring.data.jpa.learning.annotation.*;
import org.apache.logging.log4j.util.Strings;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.javers.core.Changes;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Change;
import org.javers.core.diff.Diff;
import org.javers.core.diff.ListCompareAlgorithm;
import org.javers.core.diff.changetype.container.ArrayChange;
import org.javers.core.diff.changetype.container.ContainerElementChange;
import org.javers.core.diff.changetype.container.ElementValueChange;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Ranga Bhupal
 * @version 1.0
 * @since 2020-12-29
 */
@Component
public class HibernateInterceptor extends EmptyInterceptor {


    Javers javers = JaversBuilder.javers().withListCompareAlgorithm(ListCompareAlgorithm.AS_SET).build();

    HibernateInterceptorExtensionService extensionService;
    HibernateInterceptorProperties properties;

    Map<Class, Boolean> historyEnabledMap = new HashMap<>();
    Map<Class, List> ignoreFieldsMap = new HashMap<>();
    Map<Class, String> identifierMethodMap = new HashMap<>();
    Map<Class, List> extensionFieldHookMap = new HashMap<>();

    Map<Long, List<Object>> threadMap = new HashMap<>();

    public HibernateInterceptor() {
        System.out.println("****************************************");
        System.out.println("***** HibernateInterceptor Created *****");
        System.out.println("****************************************");
    }

    private HibernateInterceptorExtensionService getExtensionService() {
        if (extensionService == null) {
            extensionService = SpringContextUtil.getApplicationContext().getBean(HibernateInterceptorExtensionService.class);
        }

        return extensionService;
    }

    private HibernateInterceptorProperties getProperties() {
        if (properties == null) {
            properties = SpringContextUtil.getApplicationContext().getBean(HibernateInterceptorProperties.class);
        }
        return properties;
    }

    @Override
    public boolean onSave(Object entity,
                          Serializable id,
                          Object[] state,
                          String[] propertyNames,
                          Type[] types) {
        if (isAuditHistoryEnabledFor(entity, AuditHistoryType.INSERT)
                && !isThreadSameAsPreviousForEntity(entity) ) {
            writeHistory(entity, state, null, propertyNames);
            clearThreadMap(entity);
        }

        return super.onSave(entity, id, state, propertyNames, types);
    }

    @Override
    public boolean onFlushDirty(Object entity,
                                Serializable id,
                                Object[] currentState,
                                Object[] previousState,
                                String[] propertyNames,
                                Type[] types) {
        if (isAuditHistoryEnabledFor(entity, AuditHistoryType.UPDATE)
                && !isThreadSameAsPreviousForEntity(entity) ){
            writeHistory(entity, currentState, previousState, propertyNames);
            clearThreadMap(entity);
        }

        return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
    }

    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        if (isAuditHistoryEnabledFor(entity, AuditHistoryType.DELETE)
                && !isThreadSameAsPreviousForEntity(entity) ) {
            writeHistory(entity, null, state, propertyNames);
            clearThreadMap(entity);
        }

        super.onDelete(entity, id, state, propertyNames, types);
    }

    private boolean isAuditHistoryEnabled(Object entity) {
        if (!historyEnabledMap.containsKey(entity.getClass())) {
            historyEnabledMap.put(
                    entity.getClass(),
                    AnnotationUtils.isAnnotationDeclaredLocally(AuditHistory.class, entity.getClass())
            );
        }

        return historyEnabledMap.getOrDefault(entity.getClass(), false);
    }

    private boolean isAuditHistoryEnabledFor(Object entity, AuditHistoryType requiredAuditHistoryType) {
        boolean isEnabled = false;
        if (isAuditHistoryEnabled(entity)) {
            AuditHistoryType[] types = entity.getClass().getAnnotation(AuditHistory.class).value();
            isEnabled = Arrays.stream(types)
                    .anyMatch(type -> type.equals(requiredAuditHistoryType) || type.equals(AuditHistoryType.ALL));
        }

        return isEnabled;
    }

    private boolean isThreadSameAsPreviousForEntity(Object entity) {
        List<Object> entityList = this.threadMap.get(Thread.currentThread().getId());
        if (entityList==null){
            entityList = new ArrayList<>();
            this.threadMap.put(Thread.currentThread().getId(), entityList);
        }

        boolean isSameThread = this.threadMap.containsKey(Thread.currentThread().getId());
        boolean isSameEntity = entityList.contains(entity);

        if (!(isSameThread && isSameEntity)) {
            entityList.add(entity);
            this.threadMap.put(Thread.currentThread().getId(), entityList);
            return false;
        }

        return true;
    }

    private boolean clearThreadMap(Object entity){
        this.threadMap.remove(entity.getClass().getName());
        return true;
    }

    private Object getEntityAuditIdentifier(Object entity, String[] propertyNames) {
        Object returnValue = null;

        if (!identifierMethodMap.containsKey(entity.getClass())) {
            List<String> identifierMethods = Arrays.stream(entity.getClass().getDeclaredMethods())
                    .filter(method -> AnnotationUtils.getAnnotation(method, AuditHistoryIdentifier.class) != null)
                    .map(method -> method.getName())
                    .collect(Collectors.toList());
            if (!identifierMethods.isEmpty()) {
                identifierMethodMap.put(entity.getClass(), identifierMethods.get(0));
            } else {
                List<String> superClassIdentifierMethods = Arrays.stream(entity.getClass().getSuperclass().getDeclaredMethods())
                        .filter(method -> AnnotationUtils.getAnnotation(method, AuditHistoryIdentifier.class) != null)
                        .map(method -> method.getName())
                        .collect(Collectors.toList());
                if (!superClassIdentifierMethods.isEmpty()){
                    identifierMethodMap.put(entity.getClass(), superClassIdentifierMethods.get(0));
                }
            }
        }

        String identifierMethod = identifierMethodMap.get(entity.getClass());
        if (identifierMethod != null && identifierMethod.length() > 0) {
            try {
                Method getIdMethod = entity.getClass().getDeclaredMethod(identifierMethod);
                returnValue = getIdMethod.invoke(entity);
            } catch (Exception excp) {
                System.err.println(excp);
                // TODO: Exception Handling
            }
        }
        return returnValue;
    }

    private List getAuditHistoryIgnoreProperties(Object entity) {
        List<String> ignoreFieldsList = new ArrayList<>();

        if (!ignoreFieldsMap.containsKey(entity.getClass())) {
            List<String> fields = Arrays.stream(entity.getClass().getDeclaredFields())
                    .filter(field -> AnnotationUtils.getAnnotation(field, AuditHistoryIgnore.class) != null)
                    .map(field -> field.getName())
                    .collect(Collectors.toList());
            if (fields != null && !fields.isEmpty()) {
                ignoreFieldsList.addAll(fields);
            }

            List<String> superClassFields = Arrays.stream(entity.getClass().getSuperclass().getDeclaredFields())
                    .filter(field -> AnnotationUtils.getAnnotation(field, AuditHistoryIgnore.class) != null)
                    .map(field -> field.getName())
                    .collect(Collectors.toList());
            if (superClassFields != null && !superClassFields.isEmpty()) {
                ignoreFieldsList.addAll(superClassFields);
            }

            ignoreFieldsMap.put(entity.getClass(), ignoreFieldsList);
        }

        return ignoreFieldsMap.getOrDefault(entity.getClass(), ignoreFieldsList);
    }

    private List getFieldExtensionsRegistered(Object entity) {
        List<String> extensionFieldList = new ArrayList<>();

        if (!extensionFieldHookMap.containsKey(entity.getClass())) {
            List<String> fields = Arrays.stream(entity.getClass().getDeclaredFields())
                    .filter(field -> AnnotationUtils.getAnnotation(field, AuditHistoryFieldExtensionHook.class) != null)
                    .map(field -> field.getName())
                    .collect(Collectors.toList());
            if (fields != null && !fields.isEmpty()) {
                extensionFieldList.addAll(fields);
            }

            List<String> superClassFields = Arrays.stream(entity.getClass().getSuperclass().getDeclaredFields())
                    .filter(field -> AnnotationUtils.getAnnotation(field, AuditHistoryFieldExtensionHook.class) != null)
                    .map(field -> field.getName())
                    .collect(Collectors.toList());
            if (superClassFields != null && !superClassFields.isEmpty()) {
                extensionFieldList.addAll(superClassFields);
            }

            extensionFieldHookMap.put(entity.getClass(), extensionFieldList);
        }

        return extensionFieldHookMap.getOrDefault(entity.getClass(), extensionFieldList);
    }

    private void writeHistory(Object entity, Object[] currentState, Object[] previousState, String[] propertyNames) {
        List<String> changeStringsList = new ArrayList<>();

        List auditHistoryIgnoreFields = getAuditHistoryIgnoreProperties(entity);
        List auditHistoryFieldExtensionHookList = getFieldExtensionsRegistered(entity);

        Object identifier = getEntityAuditIdentifier(entity, propertyNames);

        if (previousState == null && currentState != null) {
            changeStringsList.add(String.format(getProperties().getNewFormat(), entity.getClass().getSimpleName(), identifier));
        } else if (previousState != null && currentState == null) {
            changeStringsList.add(String.format(getProperties().getDeleteFormat(), entity.getClass().getSimpleName(), identifier));
        } else {
            Diff compare = javers.compare(previousState, currentState);
            Changes changes = compare.getChanges();
            for (int i = 0; i < changes.size(); i++) {
                Change change = changes.get(i);

                if (change instanceof ArrayChange) {
                    ArrayChange ac = (ArrayChange) change;
                    List<ContainerElementChange> arrayChanges = ac.getChanges();

                    for (int idx = 0; idx < arrayChanges.size(); idx++) {
                        ElementValueChange ele = (ElementValueChange) arrayChanges.get(idx);
                        String propertyName = propertyNames[ele.getIndex()];

                        if (!auditHistoryIgnoreFields.contains(propertyName)) {
                            Object previousValue = ele.getLeftValue();
                            Object currentValue = ele.getRightValue();

                            if (auditHistoryFieldExtensionHookList.contains(propertyName)) {
                                Map<Object, Object> updatedMap = getExtensionService().fieldExtension(
                                        entity,
                                        propertyName,
                                        previousValue,
                                        currentValue
                                );

                                propertyName = updatedMap.getOrDefault(propertyName, propertyName).toString();
                                previousValue = updatedMap.getOrDefault(previousValue, previousValue);
                                currentValue = updatedMap.getOrDefault(currentValue, currentValue);
                            }

                            String message = String.format(
                                    getProperties().getModifiedFormat(),
                                    propertyName,
                                    previousValue,
                                    currentValue
                            );
                            changeStringsList.add(message);
                        }
                    }
                }
            }
        }

        getExtensionService().historyExtension(
                entity, currentState, previousState, propertyNames,
                changeStringsList
        );
    }
}
