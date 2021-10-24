package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.temporal.Temporal;

import java.util.List;
import java.util.UUID;
import java.util.Objects;

import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.AuthStrategy;
import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.ModelOperation;
import com.amplifyframework.core.model.annotations.AuthRule;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the Event type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Events", authRules = {
  @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ })
})
public final class Event implements Model {
  public static final QueryField ID = field("Event", "id");
  public static final QueryField NAME = field("Event", "name");
  public static final QueryField START_LOCAL_TIME = field("Event", "startLocalTime");
  public static final QueryField END_LOCAL_TIME = field("Event", "endLocalTime");
  public static final QueryField USER_NAME = field("Event", "userName");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String", isRequired = true) String name;
  private final @ModelField(targetType="AWSDateTime", isRequired = true) Temporal.DateTime startLocalTime;
  private final @ModelField(targetType="AWSDateTime") Temporal.DateTime endLocalTime;
  private final @ModelField(targetType="String", isRequired = true) String userName;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime createdAt;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime updatedAt;
  public String getId() {
      return id;
  }
  
  public String getName() {
      return name;
  }
  
  public Temporal.DateTime getStartLocalTime() {
      return startLocalTime;
  }
  
  public Temporal.DateTime getEndLocalTime() {
      return endLocalTime;
  }
  
  public String getUserName() {
      return userName;
  }
  
  public Temporal.DateTime getCreatedAt() {
      return createdAt;
  }
  
  public Temporal.DateTime getUpdatedAt() {
      return updatedAt;
  }
  
  private Event(String id, String name, Temporal.DateTime startLocalTime, Temporal.DateTime endLocalTime, String userName) {
    this.id = id;
    this.name = name;
    this.startLocalTime = startLocalTime;
    this.endLocalTime = endLocalTime;
    this.userName = userName;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Event event = (Event) obj;
      return ObjectsCompat.equals(getId(), event.getId()) &&
              ObjectsCompat.equals(getName(), event.getName()) &&
              ObjectsCompat.equals(getStartLocalTime(), event.getStartLocalTime()) &&
              ObjectsCompat.equals(getEndLocalTime(), event.getEndLocalTime()) &&
              ObjectsCompat.equals(getUserName(), event.getUserName()) &&
              ObjectsCompat.equals(getCreatedAt(), event.getCreatedAt()) &&
              ObjectsCompat.equals(getUpdatedAt(), event.getUpdatedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getName())
      .append(getStartLocalTime())
      .append(getEndLocalTime())
      .append(getUserName())
      .append(getCreatedAt())
      .append(getUpdatedAt())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Event {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("name=" + String.valueOf(getName()) + ", ")
      .append("startLocalTime=" + String.valueOf(getStartLocalTime()) + ", ")
      .append("endLocalTime=" + String.valueOf(getEndLocalTime()) + ", ")
      .append("userName=" + String.valueOf(getUserName()) + ", ")
      .append("createdAt=" + String.valueOf(getCreatedAt()) + ", ")
      .append("updatedAt=" + String.valueOf(getUpdatedAt()))
      .append("}")
      .toString();
  }
  
  public static NameStep builder() {
      return new Builder();
  }
  
  /** 
   * WARNING: This method should not be used to build an instance of this object for a CREATE mutation.
   * This is a convenience method to return an instance of the object with only its ID populated
   * to be used in the context of a parameter in a delete mutation or referencing a foreign key
   * in a relationship.
   * @param id the id of the existing item this instance will represent
   * @return an instance of this model with only ID populated
   * @throws IllegalArgumentException Checks that ID is in the proper format
   */
  public static Event justId(String id) {
    try {
      UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
    } catch (Exception exception) {
      throw new IllegalArgumentException(
              "Model IDs must be unique in the format of UUID. This method is for creating instances " +
              "of an existing object with only its ID field for sending as a mutation parameter. When " +
              "creating a new object, use the standard builder method and leave the ID field blank."
      );
    }
    return new Event(
      id,
      null,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      name,
      startLocalTime,
      endLocalTime,
      userName);
  }
  public interface NameStep {
    StartLocalTimeStep name(String name);
  }
  

  public interface StartLocalTimeStep {
    UserNameStep startLocalTime(Temporal.DateTime startLocalTime);
  }
  

  public interface UserNameStep {
    BuildStep userName(String userName);
  }
  

  public interface BuildStep {
    Event build();
    BuildStep id(String id) throws IllegalArgumentException;
    BuildStep endLocalTime(Temporal.DateTime endLocalTime);
  }
  

  public static class Builder implements NameStep, StartLocalTimeStep, UserNameStep, BuildStep {
    private String id;
    private String name;
    private Temporal.DateTime startLocalTime;
    private String userName;
    private Temporal.DateTime endLocalTime;
    @Override
     public Event build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Event(
          id,
          name,
          startLocalTime,
          endLocalTime,
          userName);
    }
    
    @Override
     public StartLocalTimeStep name(String name) {
        Objects.requireNonNull(name);
        this.name = name;
        return this;
    }
    
    @Override
     public UserNameStep startLocalTime(Temporal.DateTime startLocalTime) {
        Objects.requireNonNull(startLocalTime);
        this.startLocalTime = startLocalTime;
        return this;
    }
    
    @Override
     public BuildStep userName(String userName) {
        Objects.requireNonNull(userName);
        this.userName = userName;
        return this;
    }
    
    @Override
     public BuildStep endLocalTime(Temporal.DateTime endLocalTime) {
        this.endLocalTime = endLocalTime;
        return this;
    }
    
    /** 
     * WARNING: Do not set ID when creating a new object. Leave this blank and one will be auto generated for you.
     * This should only be set when referring to an already existing object.
     * @param id id
     * @return Current Builder instance, for fluent method chaining
     * @throws IllegalArgumentException Checks that ID is in the proper format
     */
    public BuildStep id(String id) throws IllegalArgumentException {
        this.id = id;
        
        try {
            UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
        } catch (Exception exception) {
          throw new IllegalArgumentException("Model IDs must be unique in the format of UUID.",
                    exception);
        }
        
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String id, String name, Temporal.DateTime startLocalTime, Temporal.DateTime endLocalTime, String userName) {
      super.id(id);
      super.name(name)
        .startLocalTime(startLocalTime)
        .userName(userName)
        .endLocalTime(endLocalTime);
    }
    
    @Override
     public CopyOfBuilder name(String name) {
      return (CopyOfBuilder) super.name(name);
    }
    
    @Override
     public CopyOfBuilder startLocalTime(Temporal.DateTime startLocalTime) {
      return (CopyOfBuilder) super.startLocalTime(startLocalTime);
    }
    
    @Override
     public CopyOfBuilder userName(String userName) {
      return (CopyOfBuilder) super.userName(userName);
    }
    
    @Override
     public CopyOfBuilder endLocalTime(Temporal.DateTime endLocalTime) {
      return (CopyOfBuilder) super.endLocalTime(endLocalTime);
    }
  }
  
}
