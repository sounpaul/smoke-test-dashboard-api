#Author: Sounak Paul
Feature: Smoke Test feature

  @Test
  Scenario Outline: Validate difference of two numbers
    Given "<second_num>" is subtracted from "<first_num>"
    Then result should be "<result>"

    Examples:

      | TC | first_num | second_num | result |
      | 1  | 2         | 3          | 1      |
      | 2  | 3         | 5          | 2      |
      | 3  | 2         | 9          | 7      |

  @Test
  Scenario Outline: Validate divisibility of two numbers
    Given "<second_num>" is divided by "<first_num>"
    Then divisibility should be "<divisibility_status>"

    Examples:

      | TC | first_num | second_num | divisibility_status |
      | 1  | 2         | 10         | yes                 |
      | 2  | 3         | 16         | no                  |
      | 3  | 11        | 55         | yes                 |



