import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegistrationSuccess } from './registration-success';

describe('RegistrationSuccess', () => {
  let component: RegistrationSuccess;
  let fixture: ComponentFixture<RegistrationSuccess>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegistrationSuccess],
    }).compileComponents();

    fixture = TestBed.createComponent(RegistrationSuccess);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
