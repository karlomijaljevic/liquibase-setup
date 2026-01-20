/**
 * Copyright (C) 2026 Karlo Mijaljević
 *
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * </p>
 *
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * </p>
 *
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * </p>
 */
package xyz.mijaljevic.liquibase_setup.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xyz.mijaljevic.liquibase_setup.entity.Person;

/**
 * Service class for managing Person entities.
 *
 * @author Karlo Mijaljević
 */
@Service
@RequiredArgsConstructor
public class PersonService {
    private final EntityManager entityManager;

    @Transactional
    public void createPerson(final Person person) {
        if (person == null) {
            throw new IllegalArgumentException("Person cannot be null");
        }

        entityManager.persist(person);
    }
}