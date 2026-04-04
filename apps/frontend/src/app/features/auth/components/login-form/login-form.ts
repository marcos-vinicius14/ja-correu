import { Component, computed, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';

import { AuthService } from '../../services/auth.service';

type ActiveTab = 'login' | 'register';

const passwordMatchValidator: ValidatorFn = (group: AbstractControl): ValidationErrors | null => {
  const password = group.get('password')?.value;
  const confirm = group.get('confirmPassword')?.value;
  return password === confirm ? null : { passwordMismatch: true };
};

@Component({
  selector: 'app-login-form',
  imports: [ReactiveFormsModule],
  templateUrl: './login-form.html',
  styleUrl: './login-form.scss',
})
export class LoginForm {
  readonly #authService = inject(AuthService);

  readonly activeTab = signal<ActiveTab>('login');
  readonly passwordVisible = signal(false);
  readonly registerPasswordVisible = signal(false);
  readonly registerConfirmVisible = signal(false);
  readonly isLoading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly registerSuccess = signal(false);

  readonly greeting = computed(() =>
    this.activeTab() === 'login' ? 'Bem-vindo de volta' : 'Crie sua conta',
  );

  readonly loginForm = new FormGroup({
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(6)],
    }),
  });

  readonly registerForm = new FormGroup(
    {
      username: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(3), Validators.maxLength(30)],
      }),
      email: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.email],
      }),
      password: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(6)],
      }),
      confirmPassword: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required],
      }),
    },
    { validators: passwordMatchValidator },
  );

  setTab(tab: ActiveTab): void {
    this.activeTab.set(tab);
    this.errorMessage.set(null);
    this.registerSuccess.set(false);
    this.loginForm.reset();
    this.registerForm.reset();
  }

  togglePasswordVisibility(): void {
    this.passwordVisible.update((v) => !v);
  }

  toggleRegisterPasswordVisibility(): void {
    this.registerPasswordVisible.update((v) => !v);
  }

  toggleRegisterConfirmVisibility(): void {
    this.registerConfirmVisible.update((v) => !v);
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.#authService.login(this.loginForm.getRawValue()).subscribe({
      error: () => {
        this.errorMessage.set('Email ou senha inválidos.');
        this.isLoading.set(false);
      },
      complete: () => this.isLoading.set(false),
    });
  }

  onRegister(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const { username, email, password } = this.registerForm.getRawValue();

    this.#authService.register({ username, email, password }).subscribe({
      next: () => {
        this.registerSuccess.set(true);
        this.registerForm.reset();
        setTimeout(() => this.setTab('login'), 2000);
      },
      error: () => {
        this.errorMessage.set('Não foi possível criar a conta. Tente novamente.');
        this.isLoading.set(false);
      },
      complete: () => this.isLoading.set(false),
    });
  }
}
