import {
  AbstractControl,
  ValidationErrors,
  ValidatorFn
} from '@angular/forms';


export function passwordPolicyValidator(
  getUserId: () => string
): ValidatorFn {

  return (
    control: AbstractControl
  ): ValidationErrors | null => {



    const password =
      String(control.value ?? '');


    /*
     * Required validation is handled
     * separately by Validators.required.
     */
    if (!password) {
      return null;
    }


    /*
     * Store all validation errors here.
     */
    const errors: ValidationErrors = {};


    /*
     * Rule 1:
     * At least one alphabetic character.
     */
    if (!/[a-zA-Z]/.test(password)) {
        
      errors['alphabetRequired'] = true;
    }


    /*
     * Rule 2:
     * At least one numeric OR special character.
     */
    if (
      !/[0-9]/.test(password) &&
      !/[^a-zA-Z0-9\s]/.test(password)
    ) {
       
      errors['numberOrSpecialRequired'] = true;
    }


    /*
     * Get User ID.
     */
    const userId =
      getUserId()
        .trim()
        .toLowerCase();


    const normalizedPassword =
      password.toLowerCase();


    if (userId) {

      /*
       * Rule 3:
       * Password cannot be the same
       * as User ID.
       */
      if (
        normalizedPassword === userId
      ) {
        console.log('user id');
        errors['sameAsUserId'] = true;
      }


      /*
       * Rule 4:
       * Password cannot be the reverse
       * of User ID.
       */
      const reversedUserId =
        userId
          .split('')
          .reverse()
          .join('');


      if (
        normalizedPassword ===
        reversedUserId
      ) {
        
        errors['reverseOfUserId'] = true;
      }


      /*
       * Rule 5:
       * Password cannot be a
       * circular shift of User ID.
       */
      if (
        isCircularShift(
          normalizedPassword,
          userId
        )
      ) {

        errors['circularShiftOfUserId'] = true;
       
      }
    }


    /*
     * If there are no errors,
     * password is valid.
     */
    return Object.keys(errors).length > 0
      ? errors
      : null;
  };
}



function isCircularShift(
  password: string,
  userId: string
): boolean {

  /*
   * Circular shifts always have
   * the same length.
   */
  if (
    password.length !==
    userId.length
  ) {

    return false;
  }


  /*
   * Same User ID is handled
   * separately.
   */
  if (password === userId) {

    return false;
  }


  /*
   * Example:
   *
   * User ID = ABC123
   *
   * Combined:
   * ABC123ABC123
   *
   * Rotations such as:
   * BC123A
   * C123AB
   * 123ABC
   *
   * will exist inside this string.
   */
  const doubledUserId =
    userId + userId;


  return doubledUserId
    .slice(
      1,
      doubledUserId.length - 1
    )
    .includes(password);
}