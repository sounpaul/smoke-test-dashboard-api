#Author: Sounak Paul
Feature: Comp Test feature

  @Test
  Scenario Outline: Validate sum of two numbers
    Given "<first_num>" and "<second_num>" is added
    Then result should be "<result>"

    Examples:

      | TC | first_num | second_num | result |
      | 1  | 2         | 3          | 5      |
      | 2  | 3         | 5          | 8      |
      | 3  | 2         | 9          | 10     |

  @Test
  Scenario Outline: Validate product of two numbers
    Given "<first_num>" and "<second_num>" is multiplied
    Then result should be "<result>"

    Examples:

      | TC | first_num | second_num | result |
      | 1  | 2         | 3          | 6      |
      | 2  | 4         | 5          | 20     |
      | 3  | 5         | 10         | 50     |



