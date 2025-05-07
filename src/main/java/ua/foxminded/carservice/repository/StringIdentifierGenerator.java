package ua.foxminded.carservice.repository;

import java.util.UUID;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import ua.foxminded.carservice.domain.StringIdentifiable;

public class StringIdentifierGenerator implements IdentifierGenerator{

  @Override
  public Object generate(SharedSessionContractImplementor session, Object object) {
    if (object instanceof StringIdentifiable identifiable) {
      return identifiable.getId() == null ? UUID.randomUUID().toString() : identifiable.getId();     
    } else {
      return null;
    }
  }
}
