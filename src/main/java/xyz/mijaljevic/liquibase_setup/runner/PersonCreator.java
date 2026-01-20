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
package xyz.mijaljevic.liquibase_setup.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import xyz.mijaljevic.liquibase_setup.entity.Person;
import xyz.mijaljevic.liquibase_setup.service.PersonService;

/**
 * Application runner that creates a specified number of Person entities
 * with random names and ages upon application startup.
 *
 * @author Karlo Mijaljević
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PersonCreator implements ApplicationRunner {
    /**
     * Service for managing Person entities
     */
    private final PersonService personService;

    /**
     * Number of persons to create on application startup
     */
    private static final int NUMBER_OF_PERSONS_TO_CREATE = 1000;

    /**
     * Sample names for generating random persons
     */
    private static final String[] NAMES = {
            "Alice", "Bob", "Charlie", "Diana", "Ethan",
            "Fiona", "George", "Hannah", "Ian", "Julia"
    };

    /**
     * Sample surnames for generating random persons
     */
    private static final String[] SURNAMES = {
            "Smith", "Johnson", "Williams", "Brown", "Jones",
            "Garcia", "Miller", "Davis", "Rodriguez", "Martinez"
    };

    /**
     * Minimum age for generated persons
     */
    private static final int MIN_AGE = 18;

    /**
     * Maximum age for generated persons
     */
    private static final int MAX_AGE = 65;

    @Override
    public void run(@NonNull final ApplicationArguments args) {
        log.info("Creating {} random persons...", NUMBER_OF_PERSONS_TO_CREATE);

        for (int i = 0; i < NUMBER_OF_PERSONS_TO_CREATE; i++) {
            final String name = NAMES[(int) (Math.random() * NAMES.length)]
                    + " "
                    + SURNAMES[(int) (Math.random() * SURNAMES.length)];

            final int age = MIN_AGE + (int) (Math.random() * (MAX_AGE - MIN_AGE + 1));

            final Person person = new Person();

            person.setName(name);
            person.setAge(age);

            personService.createPerson(person);
            log.info("Created person: {}", person);
        }

        log.info("Finished creating random persons.");
    }
}
