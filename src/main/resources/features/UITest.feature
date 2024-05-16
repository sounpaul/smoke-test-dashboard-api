#Author: Sounak Paul
Feature: UI Test feature

  @Test
  Scenario Outline: Validate ceiling function of a decimal number
    Given math "<function>" function is applied on "<num>"
    Then result should be "<result>"

    Examples:

      | TC | num   | result | function |
      | 1  | 23.51 | 24.0   | ceiling  |
      | 2  | 35.41 | 36.0   | ceiling  |
      | 3  | 59.78 | 60.0   | ceiling  |

  @Test
  Scenario Outline: Validate floor function of a decimal number
    Given math "<function>" function is applied on "<num>"
    Then result should be "<result>"

    Examples:

      | TC | num   | result | function |
      | 1  | 23.51 | 23.0   | floor    |
      | 2  | 35.41 | 35.0   | floor    |
      | 3  | 59.78 | 59.0   | floor    |



