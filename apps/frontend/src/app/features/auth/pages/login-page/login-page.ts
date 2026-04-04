import { Component } from '@angular/core';

import { LoginForm } from '../../components/login-form/login-form';
import { LoginHero } from '../../components/login-hero/login-hero';

@Component({
  selector: 'app-login-page',
  imports: [LoginHero, LoginForm],
  templateUrl: './login-page.html',
  styleUrl: './login-page.scss',
})
export class LoginPage {}
