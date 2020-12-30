package com.ranga.spring.data.jpa.learning.interceptor;

import com.ranga.spring.data.jpa.learning.SpringContextUtil;
import com.ranga.spring.data.jpa.learning.annotation.AuditHistory;
import com.ranga.spring.data.jpa.learning.annotation.AuditHistoryIdentifier;
import com.ranga.spring.data.jpa.learning.annotation.AuditHistoryIgnore;
import com.ranga.spring.data.jpa.learning.annotation.AuditHistoryType;
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
    Map<Class, List> ignorePropertiesMap = new HashMap<>();
    Map<Class, String> identifierMethodMap = new HashMap<>();

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
        if (isAuditHistoryEnabledFor(entity, AuditHistoryType.INSERT)) {
            writeHistory(entity, state, null, propertyNames);
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
        if (isAuditHistoryEnabledFor(entity, AuditHistoryType.UPDATE)) {
            writeHistory(entity, currentState, previousState, propertyNames);
        }

        return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
    }

    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        if (isAuditHistoryEnabledFor(entity, AuditHistoryType.DELETE)) {
            writeHistory(entity, null, state, propertyNames);
        }

        super.onDelete(entity, id, state, propertyNames, types);
    }

    private boolean isAuditHistoryEnabled(Object entity) {
        return AnnotationUtils.isAnnotationDeclaredLocally(AuditHistory.class, entity.getClass());
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

    private Object getEntityAuditIdentifier(Object entity, String[] propertyNames) {
        Object returnValue = null;

        if (!identifierMethodMap.containsKey(entity.getClass())) {
            List<String> identifierMethods = Arrays.stream(entity.getClass().getDeclaredMethods())
                    .filter(method -> AnnotationUtils.getAnnotation(method, AuditHistoryIdentifier.class) != null)
                    .map(method -> method.getName())
                    .collect(Collectors.toList());
            identifierMethodMap.put(entity.getClass(), identifierMethods.get(0));
        }

        String identifierMethod = identifierMethodMap.get(entity.getClass());
        if (Strings.isNotEmpty(identifierMethod)) {
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
        List<String> ignoreProperties = new ArrayList<>();

        if (!ignorePropertiesMap.containsKey(entity.getClass())) {
            ignoreProperties = Arrays.stream(entity.getClass().getDeclaredFields())
                    .filter(field -> AnnotationUtils.getAnnotation(field, AuditHistoryIgnore.class) != null)
                    .map(field -> field.getName())
                    .collect(Collectors.toList());
            ignorePropertiesMap.put(entity.getClass(), ignoreProperties);
        }

        return ignorePropertiesMap.get(entity.getClass());
    }

    private void writeHistory(Object entity, Object[] currentState, Object[] previousState, String[] propertyNames) {
        List<String> changeStringsList = new ArrayList<>();

        List auditHistoryIgnoreProperties = getAuditHistoryIgnoreProperties(entity);
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

                        if (!auditHistoryIgnoreProperties.contains(propertyName)) {
                            String message = String.format(getProperties().getModifiedFormat(),
                                    propertyName,
                                    ele.getLeftValue(),
                                    ele.getRightValue());
                            changeStringsList.add(message);
                        }
                    }
                }
            }
        }

        getExtensionService().writeHistoryExtension(
                entity, currentState, previousState, propertyNames,
                changeStringsList
        );

    }
}
