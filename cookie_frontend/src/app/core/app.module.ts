import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import {
  HTTP_INTERCEPTORS,
  HttpClientModule,
  HttpClientXsrfModule,
} from '@angular/common/http';

import { AppRoutingModule } from './routing/app-routing.module';
import { AppComponent } from './app.component';
import { NavbarComponent } from './components/navbar/navbar.component';
import { DashboardModule } from '../modules/dashboard/dashboard.module';
import { LoginFormModule } from '../modules/login-form/login-form.module';
import { RegistrationFormModule } from '../modules/registration-form/registration-form.module';
import { HttpRequestInterceptor } from './interceptors/http-interceptor';
import { GroupModule } from '../modules/group/group.module';
import { PantryModule } from '../modules/pantries/pantries.module';
import { ShoppingListsModule } from '../modules/shopping-lists/shopping-lists.module';
import { RecipesModule } from '../modules/recipes/recipes.module';
import { MealsModule } from '../modules/meals/meals.module';

@NgModule({
  declarations: [AppComponent, NavbarComponent],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MatButtonModule,
    MatMenuModule,
    HttpClientModule,
    HttpClientXsrfModule.withOptions({
      cookieName: 'XSRF-TOKEN',
      headerName: 'X-XSRF-TOKEN',
    }),
    DashboardModule,
    LoginFormModule,
    RegistrationFormModule,
    PantryModule,
    GroupModule,
    ShoppingListsModule,
    RecipesModule,
    MealsModule,
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpRequestInterceptor,
      multi: true,
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
