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

/** This is an auto generated class representing the Measurement type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Measurements", authRules = {
  @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ })
})
public final class Measurement implements Model {
  public static final QueryField ID = field("Measurement", "id");
  public static final QueryField NAME = field("Measurement", "name");
  public static final QueryField VALUE = field("Measurement", "value");
  public static final QueryField TIMESTAMP = field("Measurement", "timestamp");
  public static final QueryField USERNAME = field("Measurement", "username");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String", isRequired = true) String name;
  private final @ModelField(targetType="Float", isRequired = true) Double value;
  private final @ModelField(targetType="AWSTimestamp", isRequired = true) Temporal.Timestamp timestamp;
  private final @ModelField(targetType="String", isRequired = true) String username;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime createdAt;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime updatedAt;
  public String getId() {
      return id;
  }
  
  public String getName() {
      return name;
  }
  
  public Double getValue() {
      return value;
  }
  
  public Temporal.Timestamp getTimestamp() {
      return timestamp;
  }
  
  public String getUsername() {
      return username;
  }
  
  public Temporal.DateTime getCreatedAt() {
      return createdAt;
  }
  
  public Temporal.DateTime getUpdatedAt() {
      return updatedAt;
  }
  
  private Measurement(String id, String name, Double value, Temporal.Timestamp timestamp, String username) {
    this.id = id;
    this.name = name;
    this.value = value;
    this.timestamp = timestamp;
    this.username = username;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Measurement measurement = (Measurement) obj;
      return ObjectsCompat.equals(getId(), measurement.getId()) &&
              ObjectsCompat.equals(getName(), measurement.getName()) &&
              ObjectsCompat.equals(getValue(), measurement.getValue()) &&
              ObjectsCompat.equals(getTimestamp(), measurement.getTimestamp()) &&
              ObjectsCompat.equals(getUsername(), measurement.getUsername()) &&
              ObjectsCompat.equals(getCreatedAt(), measurement.getCreatedAt()) &&
              ObjectsCompat.equals(getUpdatedAt(), measurement.getUpdatedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getName())
      .append(getValue())
      .append(getTimestamp())
      .append(getUsername())
      .append(getCreatedAt())
      .append(getUpdatedAt())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Measurement {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("name=" + String.valueOf(getName()) + ", ")
      .append("value=" + String.valueOf(getValue()) + ", ")
      .append("timestamp=" + String.valueOf(getTimestamp()) + ", ")
      .append("username=" + String.valueOf(getUsername()) + ", ")
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
  public static Measurement justId(String id) {
    try {
      UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
    } catch (Exception exception) {
      throw new IllegalArgumentException(
              "Model IDs must be unique in the format of UUID. This method is for creating instances " +
              "of an existing object with only its ID field for sending as a mutation parameter. When " +
              "creating a new object, use the standard builder method and leave the ID field blank."
      );
    }
    return new Measurement(
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
      value,
      timestamp,
      username);
  }
  public interface NameStep {
    ValueStep name(String name);
  }
  

  public interface ValueStep {
    TimestampStep value(Double value);
  }
  

  public interface TimestampStep {
    UsernameStep timestamp(Temporal.Timestamp timestamp);
  }
  

  public interface UsernameStep {
    BuildStep username(String username);
  }
  

  public interface BuildStep {
    Measurement build();
    BuildStep id(String id) throws IllegalArgumentException;
  }
  

  public static class Builder implements NameStep, ValueStep, TimestampStep, UsernameStep, BuildStep {
    private String id;
    private String name;
    private Double value;
    private Temporal.Timestamp timestamp;
    private String username;
    @Override
     public Measurement build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Measurement(
          id,
          name,
          value,
          timestamp,
          username);
    }
    
    @Override
     public ValueStep name(String name) {
        Objects.requireNonNull(name);
        this.name = name;
        return this;
    }
    
    @Override
     public TimestampStep value(Double value) {
        Objects.requireNonNull(value);
        this.value = value;
        return this;
    }
    
    @Override
     public UsernameStep timestamp(Temporal.Timestamp timestamp) {
        Objects.requireNonNull(timestamp);
        this.timestamp = timestamp;
        return this;
    }
    
    @Override
     public BuildStep username(String username) {
        Objects.requireNonNull(username);
        this.username = username;
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
    private CopyOfBuilder(String id, String name, Double value, Temporal.Timestamp timestamp, String username) {
      super.id(id);
      super.name(name)
        .value(value)
        .timestamp(timestamp)
        .username(username);
    }
    
    @Override
     public CopyOfBuilder name(String name) {
      return (CopyOfBuilder) super.name(name);
    }
    
    @Override
     public CopyOfBuilder value(Double value) {
      return (CopyOfBuilder) super.value(value);
    }
    
    @Override
     public CopyOfBuilder timestamp(Temporal.Timestamp timestamp) {
      return (CopyOfBuilder) super.timestamp(timestamp);
    }
    
    @Override
     public CopyOfBuilder username(String username) {
      return (CopyOfBuilder) super.username(username);
    }
  }
  
}
