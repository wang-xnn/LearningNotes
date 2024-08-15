### 1、`PropertyEditorRegistrar`类

这个类的作用是作为一种策略，将自定义或者说是定制化的`PropertyEditor`添加到`PropertyEditorRegistry`中，即将编辑器注册到编辑器注册表中，如果需要在几种不同的情况下，使用同一组属性编辑器时，这非常有用

对于`registerCustomEditors`方法，`PropertyEditorRegistry`通常是`BeanWrapper`和`DataBinder`这2种类型，在具体实现过程中，需要创建全新的`PropertyEditors`对象，因为`PropertyEditors`对象不是线程安全的

```Java
package org.springframework.beans;

public interface PropertyEditorRegistrar {
	
	void registerCustomEditors(PropertyEditorRegistry registry);

}

```

#### ResourceEditorRegistrar

```Java
package org.springframework.beans.support;

public class ResourceEditorRegistrar implements PropertyEditorRegistrar {

    private final PropertyResolver propertyResolver;

    private final ResourceLoader resourceLoader;

    public ResourceEditorRegistrar(ResourceLoader resourceLoader, PropertyResolver propertyResolver) {
       this.resourceLoader = resourceLoader;
       this.propertyResolver = propertyResolver;
    }
		
  	// 填充编辑器到注册表中
    @Override
    public void registerCustomEditors(PropertyEditorRegistry registry) {
       ResourceEditor baseEditor = new ResourceEditor(this.resourceLoader, this.propertyResolver);
       doRegisterEditor(registry, Resource.class, baseEditor);
       doRegisterEditor(registry, ContextResource.class, baseEditor);
       doRegisterEditor(registry, WritableResource.class, baseEditor);
       doRegisterEditor(registry, InputStream.class, new InputStreamEditor(baseEditor));
       doRegisterEditor(registry, InputSource.class, new InputSourceEditor(baseEditor));
       doRegisterEditor(registry, File.class, new FileEditor(baseEditor));
       doRegisterEditor(registry, Path.class, new PathEditor(baseEditor));
       doRegisterEditor(registry, Reader.class, new ReaderEditor(baseEditor));
       doRegisterEditor(registry, URL.class, new URLEditor(baseEditor));

       ClassLoader classLoader = this.resourceLoader.getClassLoader();
       doRegisterEditor(registry, URI.class, new URIEditor(classLoader));
       doRegisterEditor(registry, Class.class, new ClassEditor(classLoader));
       doRegisterEditor(registry, Class[].class, new ClassArrayEditor(classLoader));

       if (this.resourceLoader instanceof ResourcePatternResolver) {
          doRegisterEditor(registry, Resource[].class,
                new ResourceArrayPropertyEditor((ResourcePatternResolver) this.resourceLoader, this.propertyResolver));
       }
    }

 		// 如果可能的话，覆盖默认编辑器（因为这就是我们在这里真正想要做的）;否则，注册为自定义编辑器。
    private void doRegisterEditor(PropertyEditorRegistry registry, Class<?> requiredType, PropertyEditor editor) {
       if (registry instanceof PropertyEditorRegistrySupport) {
          ((PropertyEditorRegistrySupport) registry).overrideDefaultEditor(requiredType, editor);
       }
       else {
          registry.registerCustomEditor(requiredType, editor);
       }
    }

}
```

### 2、`PropertyEditor`类

最重要的2个方法是`getAsText`和`setAsText`方法，自定义一个编辑器需要实现这`setAsText`个方法

#### PropertyEditor

```Java
package java.beans;


public interface PropertyEditor {

    void setValue(Object value);

    Object getValue();

    String getAsText();

    void setAsText(String text) throws java.lang.IllegalArgumentException;

    String[] getTags();

    boolean supportsCustomEditor();
  	
  	...

}

```

#### PropertyEditorSupport

```Java
package java.beans;

import java.beans.*;

// 这是一个支持类，用于帮助构建属性编辑器。
// 它既可以用作基类，也可以用作委托类
public class PropertyEditorSupport implements PropertyEditor {
    private Object value;
    private Object source;
    private java.util.Vector<PropertyChangeListener> listeners;

    // 设置将要被编辑的value
    public void setValue(Object value) {
        this.value = value;
        firePropertyChange();
    }

    // 获取将要被编辑的value
    public Object getValue() {
        return value;
    }

    // 将value转换成String类型
    public String getAsText() {
        return (this.value != null)
                ? this.value.toString()
                : null;
    }

    // 处理String类型的对象，进行编辑处理，最后放入value中
    public void setAsText(String text) throws java.lang.IllegalArgumentException {
        if (value instanceof String) {
            setValue(text);
            return;
        }
        throw new java.lang.IllegalArgumentException(text);
    }

    public void firePropertyChange() {
        java.util.Vector<PropertyChangeListener> targets;
        synchronized (this) {
            if (listeners == null) {
                return;
            }
            targets = unsafeClone(listeners);
        }
        // Tell our listeners that "everything" has changed.
        PropertyChangeEvent evt = new PropertyChangeEvent(source, null, null, null);

        for (int i = 0; i < targets.size(); i++) {
            PropertyChangeListener target = targets.elementAt(i);
            target.propertyChange(evt);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> java.util.Vector<T> unsafeClone(java.util.Vector<T> v) {
        return (java.util.Vector<T>) v.clone();
    }

}
```

#### AddressPropertyEditor

```Java
import java.beans.PropertyEditorSupport;

public class AddressPropertyEditor extends PropertyEditorSupport {
 
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        String[] s = text.split("_");
        Address address = new Address();
        address.setProvince(s[0]);
        address.setCity(s[1]);
        address.setStreet(s[2]);
        setValue(address);
    }
}
```

### 3、PropertyEditorRegistry类

作为一个注册表，这个接口的实现类是作为属性编辑器的存储表的，跟`ProperyEditor`一样，同样有一个基础实现类，`PropertyEditorRegistrySupport`

#### PropertyEditorRegistry

```Java
package org.springframework.beans;

import java.beans.PropertyEditor;

import org.springframework.lang.Nullable;


public interface PropertyEditorRegistry {

    void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor);

   
    void registerCustomEditor(@Nullable Class<?> requiredType, @Nullable String propertyPath, PropertyEditor propertyEditor);

    @Nullable
    PropertyEditor findCustomEditor(@Nullable Class<?> requiredType, @Nullable String propertyPath);

}
```

#### PropertyEditorRegistrySupport

在这个类中，defaultEditors和overriddenDefaultEditors放的是默认编辑器，overriddenDefaultEditors通过overrideDefaultEditor这个方法放入，defaultEditors则是通过createDefaultEditors方法初始化，放入一些基本类型的编辑器，想要查询获取这2个存储里的编辑器，需要先调用registerDefaultEditors方法，将defaultEditorsActive字段设置为true，然后通过getDefaultEditor方法先查overriddenDefaultEditors，再查defaultEditors

对于定制化的属性编辑器，是customEditors和customEditorsForPath这2处存储，通过registerCustomEditor方法，判读字段propertyPath是否为空，为空就存入customEditors，不为空就存入customEditorsForPath

对于customEditorCache，只在getCustomEditor相关的重载方法，只有一个requiredType时用到，在customEditors里找不到相关requiredType类型的编辑器时，就找requiredType的父类，判断是否父类相对应的属性编辑器，如果存在，放入customEditorCache缓存中，然后返回

```Java
package org.springframework.beans;

public class PropertyEditorRegistrySupport implements PropertyEditorRegistry {

    private static final boolean shouldIgnoreXml = SpringProperties.getFlag("spring.xml.ignore");

    @Nullable
    private ConversionService conversionService;

    private boolean defaultEditorsActive = false;

    private boolean configValueEditorsActive = false;

    @Nullable
    private Map<Class<?>, PropertyEditor> defaultEditors;

    @Nullable
    private Map<Class<?>, PropertyEditor> overriddenDefaultEditors;

    @Nullable
    private Map<Class<?>, PropertyEditor> customEditors;

    @Nullable
    private Map<String, CustomEditorHolder> customEditorsForPath;

    @Nullable
    private Map<Class<?>, PropertyEditor> customEditorCache;
  
  	protected void registerDefaultEditors() {
      this.defaultEditorsActive = true;
    }

  
    public void overrideDefaultEditor(Class<?> requiredType, PropertyEditor propertyEditor) {
       if (this.overriddenDefaultEditors == null) {
          this.overriddenDefaultEditors = new HashMap<>();
       }
       this.overriddenDefaultEditors.put(requiredType, propertyEditor);
    }

  
    @Nullable
    public PropertyEditor getDefaultEditor(Class<?> requiredType) {
       if (!this.defaultEditorsActive) {
          return null;
       }
       if (this.overriddenDefaultEditors != null) {
          PropertyEditor editor = this.overriddenDefaultEditors.get(requiredType);
          if (editor != null) {
             return editor;
          }
       }
       if (this.defaultEditors == null) {
          createDefaultEditors();
       }
       return this.defaultEditors.get(requiredType);
    }


    private void createDefaultEditors() {
       this.defaultEditors = new HashMap<>(64);
       this.defaultEditors.put(Charset.class, new CharsetEditor());
       this.defaultEditors.put(Class.class, new ClassEditor());
       this.defaultEditors.put(Class[].class, new ClassArrayEditor());
       this.defaultEditors.put(Currency.class, new CurrencyEditor());
       this.defaultEditors.put(File.class, new FileEditor());
       this.defaultEditors.put(InputStream.class, new InputStreamEditor());
       if (!shouldIgnoreXml) {
          this.defaultEditors.put(InputSource.class, new InputSourceEditor());
       }
       this.defaultEditors.put(Locale.class, new LocaleEditor());
       this.defaultEditors.put(Path.class, new PathEditor());
       this.defaultEditors.put(Pattern.class, new PatternEditor());
       this.defaultEditors.put(Properties.class, new PropertiesEditor());
       this.defaultEditors.put(Reader.class, new ReaderEditor());
       this.defaultEditors.put(Resource[].class, new ResourceArrayPropertyEditor());
       this.defaultEditors.put(TimeZone.class, new TimeZoneEditor());
       this.defaultEditors.put(URI.class, new URIEditor());
       this.defaultEditors.put(URL.class, new URLEditor());
       this.defaultEditors.put(UUID.class, new UUIDEditor());
       this.defaultEditors.put(ZoneId.class, new ZoneIdEditor());

       // Default instances of collection editors.
       // Can be overridden by registering custom instances of those as custom editors.
       this.defaultEditors.put(Collection.class, new CustomCollectionEditor(Collection.class));
       this.defaultEditors.put(Set.class, new CustomCollectionEditor(Set.class));
       this.defaultEditors.put(SortedSet.class, new CustomCollectionEditor(SortedSet.class));
       this.defaultEditors.put(List.class, new CustomCollectionEditor(List.class));
       this.defaultEditors.put(SortedMap.class, new CustomMapEditor(SortedMap.class));

       // Default editors for primitive arrays.
       this.defaultEditors.put(byte[].class, new ByteArrayPropertyEditor());
       this.defaultEditors.put(char[].class, new CharArrayPropertyEditor());

       // The JDK does not contain a default editor for char!
       this.defaultEditors.put(char.class, new CharacterEditor(false));
       this.defaultEditors.put(Character.class, new CharacterEditor(true));

       // Spring's CustomBooleanEditor accepts more flag values than the JDK's default editor.
       this.defaultEditors.put(boolean.class, new CustomBooleanEditor(false));
       this.defaultEditors.put(Boolean.class, new CustomBooleanEditor(true));

       // The JDK does not contain default editors for number wrapper types!
       // Override JDK primitive number editors with our own CustomNumberEditor.
       this.defaultEditors.put(byte.class, new CustomNumberEditor(Byte.class, false));
       this.defaultEditors.put(Byte.class, new CustomNumberEditor(Byte.class, true));
       this.defaultEditors.put(short.class, new CustomNumberEditor(Short.class, false));
       this.defaultEditors.put(Short.class, new CustomNumberEditor(Short.class, true));
       this.defaultEditors.put(int.class, new CustomNumberEditor(Integer.class, false));
       this.defaultEditors.put(Integer.class, new CustomNumberEditor(Integer.class, true));
       this.defaultEditors.put(long.class, new CustomNumberEditor(Long.class, false));
       this.defaultEditors.put(Long.class, new CustomNumberEditor(Long.class, true));
       this.defaultEditors.put(float.class, new CustomNumberEditor(Float.class, false));
       this.defaultEditors.put(Float.class, new CustomNumberEditor(Float.class, true));
       this.defaultEditors.put(double.class, new CustomNumberEditor(Double.class, false));
       this.defaultEditors.put(Double.class, new CustomNumberEditor(Double.class, true));
       this.defaultEditors.put(BigDecimal.class, new CustomNumberEditor(BigDecimal.class, true));
       this.defaultEditors.put(BigInteger.class, new CustomNumberEditor(BigInteger.class, true));

       // Only register config value editors if explicitly requested.
       if (this.configValueEditorsActive) {
          StringArrayPropertyEditor sae = new StringArrayPropertyEditor();
          this.defaultEditors.put(String[].class, sae);
          this.defaultEditors.put(short[].class, sae);
          this.defaultEditors.put(int[].class, sae);
          this.defaultEditors.put(long[].class, sae);
       }
    }



    @Override
    public void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor) {
       registerCustomEditor(requiredType, null, propertyEditor);
    }

    @Override
    public void registerCustomEditor(@Nullable Class<?> requiredType, @Nullable String propertyPath, PropertyEditor propertyEditor) {
       if (requiredType == null && propertyPath == null) {
          throw new IllegalArgumentException("Either requiredType or propertyPath is required");
       }
       if (propertyPath != null) {
          if (this.customEditorsForPath == null) {
             this.customEditorsForPath = new LinkedHashMap<>(16);
          }
          this.customEditorsForPath.put(propertyPath, new CustomEditorHolder(propertyEditor, requiredType));
       }
       else {
         	// requiredType不为空
          if (this.customEditors == null) {
             this.customEditors = new LinkedHashMap<>(16);
          }
          this.customEditors.put(requiredType, propertyEditor);
          this.customEditorCache = null;
       }
    }

    @Override
    @Nullable
    public PropertyEditor findCustomEditor(@Nullable Class<?> requiredType, @Nullable String propertyPath) {
       Class<?> requiredTypeToUse = requiredType;
       if (propertyPath != null) {
          if (this.customEditorsForPath != null) {
             // Check property-specific editor first.
             PropertyEditor editor = getCustomEditor(propertyPath, requiredType);
             if (editor == null) {
                List<String> strippedPaths = new ArrayList<>();
                addStrippedPropertyPaths(strippedPaths, "", propertyPath);
                for (Iterator<String> it = strippedPaths.iterator(); it.hasNext() && editor == null;) {
                   String strippedPath = it.next();
                   editor = getCustomEditor(strippedPath, requiredType);
                }
             }
             if (editor != null) {
                return editor;
             }
          }
          if (requiredType == null) {
             requiredTypeToUse = getPropertyType(propertyPath);
          }
       }
			
       return getCustomEditor(requiredTypeToUse);
    }


    public boolean hasCustomEditorForElement(@Nullable Class<?> elementType, @Nullable String propertyPath) {
       if (propertyPath != null && this.customEditorsForPath != null) {
          for (Map.Entry<String, CustomEditorHolder> entry : this.customEditorsForPath.entrySet()) {
             if (PropertyAccessorUtils.matchesProperty(entry.getKey(), propertyPath) &&
                   entry.getValue().getPropertyEditor(elementType) != null) {
                return true;
             }
          }
       }
       // No property-specific editor -> check type-specific editor.
       return (elementType != null && this.customEditors != null && this.customEditors.containsKey(elementType));
    }
  
    @Nullable
    protected Class<?> getPropertyType(String propertyPath) {
       return null;
    }

    @Nullable
    private PropertyEditor getCustomEditor(String propertyName, @Nullable Class<?> requiredType) {
       CustomEditorHolder holder =
             (this.customEditorsForPath != null ? this.customEditorsForPath.get(propertyName) : null);
       return (holder != null ? holder.getPropertyEditor(requiredType) : null);
    }

 
    @Nullable
    private PropertyEditor getCustomEditor(@Nullable Class<?> requiredType) {
       if (requiredType == null || this.customEditors == null) {
          return null;
       }
       // Check directly registered editor for type.
       PropertyEditor editor = this.customEditors.get(requiredType);
       if (editor == null) {
          // Check cached editor for type, registered for superclass or interface.
          if (this.customEditorCache != null) {
             editor = this.customEditorCache.get(requiredType);
          }
          if (editor == null) {
             // Find editor for superclass or interface.
             for (Map.Entry<Class<?>, PropertyEditor> entry : this.customEditors.entrySet()) {
                if (editor != null) {
                   break;
                }
                Class<?> key = entry.getKey();
                if (key.isAssignableFrom(requiredType)) {
                   editor = entry.getValue();
                   if (this.customEditorCache == null) {
                      this.customEditorCache = new HashMap<>();
                   }
                   this.customEditorCache.put(requiredType, editor);
                }
             }
          }
       }
       return editor;
    }

   
    private static final class CustomEditorHolder {

       private final PropertyEditor propertyEditor;

       @Nullable
       private final Class<?> registeredType;

       private CustomEditorHolder(PropertyEditor propertyEditor, @Nullable Class<?> registeredType) {
          this.propertyEditor = propertyEditor;
          this.registeredType = registeredType;
       }

       private PropertyEditor getPropertyEditor() {
          return this.propertyEditor;
       }

       @Nullable
       private Class<?> getRegisteredType() {
          return this.registeredType;
       }

       @Nullable
       private PropertyEditor getPropertyEditor(@Nullable Class<?> requiredType) {
          if (this.registeredType == null ||
                (requiredType != null &&
                (ClassUtils.isAssignable(this.registeredType, requiredType) ||
                ClassUtils.isAssignable(requiredType, this.registeredType))) ||
                (requiredType == null &&
                (!Collection.class.isAssignableFrom(this.registeredType) && !this.registeredType.isArray()))) {
             return this.propertyEditor;
          }
          else {
             return null;
          }
       }
    }

}
```

### 4、自定义属性编辑器

如果我们要自定义一个属性编辑，并把它放入注册表中，方便BeanFactory使用，需要做到三点，

```Java
import lombok.Data;

@Data
public class Address {
    private String province;
    private String city;
    private String street;
}
```

a) 先定义一个自定义属性编辑器，可以实现基本类PropertyEditorSuppoer，这样就只用重写setAsText方法

```Java
import java.beans.PropertyEditorSupport;

public class AddressPropertyEditor extends PropertyEditorSupport {
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        String[] s = text.split("_");
        Address address = new Address();
        address.setProvince(s[0]);
        address.setCity(s[1]);
        address.setStreet(s[2]);
        setValue(address);
    }
}
```

b）把这个自定义属性编辑器，放入容器的注册表内，而这个工作是通过PropertyEditorRegistrar类来实现的，可以像ResourceEditorRegistrar一样，实现PropertyEditorRegistrar接口，把自定义的编辑器放入属性注册表中

```Java
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.validation.DataBinder;

import java.beans.PropertyEditor;

public class AddressPropertyRegistrar implements PropertyEditorRegistrar {
    @Override
    public void registerCustomEditors(PropertyEditorRegistry registry) {
        registry.registerCustomEditor(Address.class, new AddressPropertyEditor());
    }
}
```

c)  把实现的Regitrar类，放入Spring容器内

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd">
    <bean id="customer" class="com.test.selfEditor.Customer">
        <property name="address" value="江西省_南昌市_红谷滩区"/>
        <property name="name" value="richard"/>
    </bean>
    <bean class="org.springframework.beans.factory.config.CustomEditorConfigurer">
            <property name="propertyEditorRegistrars">
                <list>
                    <bean class="com.test.selfEditor.AddressPropertyRegistrar"/>
                </list>
            </property>
    </bean>
</beans>
```

 CustomEditorConfigurer类实现了BeanFactoryPostProcessor接口，在容器初始化时，会执行postProcessBeanFactory方法，把自定义的propertyEditorRegistrars或者customEditors放入BeanFactory中

```Java
package org.springframework.beans.factory.config;

import java.beans.PropertyEditor;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

public class CustomEditorConfigurer implements BeanFactoryPostProcessor, Ordered {

    protected final Log logger = LogFactory.getLog(getClass());

    private int order = Ordered.LOWEST_PRECEDENCE;  // default: same as non-Ordered

    @Nullable
    private PropertyEditorRegistrar[] propertyEditorRegistrars;

    @Nullable
    private Map<Class<?>, Class<? extends PropertyEditor>> customEditors;


    public void setOrder(int order) {
       this.order = order;
    }

    @Override
    public int getOrder() {
       return this.order;
    }

    public void setPropertyEditorRegistrars(PropertyEditorRegistrar[] propertyEditorRegistrars) {
       this.propertyEditorRegistrars = propertyEditorRegistrars;
    }

    public void setCustomEditors(Map<Class<?>, Class<? extends PropertyEditor>> customEditors) {
       this.customEditors = customEditors;
    }


    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
       if (this.propertyEditorRegistrars != null) {
          for (PropertyEditorRegistrar propertyEditorRegistrar : this.propertyEditorRegistrars) {
             beanFactory.addPropertyEditorRegistrar(propertyEditorRegistrar);
          }
       }
       if (this.customEditors != null) {
          this.customEditors.forEach(beanFactory::registerCustomEditor);
       }
    }

}
```

### 5、Spring在哪些地方会用到属性编辑器

TODO
