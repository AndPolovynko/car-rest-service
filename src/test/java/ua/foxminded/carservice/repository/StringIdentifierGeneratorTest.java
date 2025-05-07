package ua.foxminded.carservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.junit.jupiter.api.Test;

import ua.foxminded.carservice.domain.StringIdentifiable;

public class StringIdentifierGeneratorTest {
  SharedSessionContractImplementor session = null;
  StringIdentifierGenerator generator = new StringIdentifierGenerator();

  @Test
  void generateShouldReturnGeneratedIdWhenIdIsNull() {
    StringIdentifiable identifiable = new TestStringIdentifiable(null);

    Object result = generator.generate(session, identifiable);

    assertThat(result).isNotNull().isInstanceOf(String.class);
  }

  @Test
  void generateShouldReturnProvidedIdWhenIdIsNotNull() {
    StringIdentifiable identifiable = new TestStringIdentifiable("existing-id");

    Object result = generator.generate(session, identifiable);

    assertThat(result).isEqualTo("existing-id");
  }

  @Test
  void generateShouldReturnNullWhenObjectIsNotStringIdentifiable() {
    Object object = new Object();

    Object result = generator.generate(session, object);

    assertThat(result).isNull();
  }
  

  private class TestStringIdentifiable implements StringIdentifiable {
    private String id;

    public TestStringIdentifiable(String id) {
      this.id = id;
    }

    @Override
    public String getId() {
      return id;
    }
  }
}
