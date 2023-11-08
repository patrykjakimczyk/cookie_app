import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-success-banner',
  template: ` <div class="container">
    <p class="header">
      {{ headerText }} <mat-icon *ngIf="iconName">{{ iconName }}</mat-icon>
    </p>
    <span>
      {{ mainText }}
    </span>
    <button mat-raised-button color="primary" [routerLink]="routerLink">
      {{ buttonText }}
    </button>
  </div>`,
  styleUrls: ['./success-banner.component.scss'],
})
export class SuccessBannerComponent {
  @Input({ required: true }) headerText!: string;
  @Input({ required: false }) iconName?: string;
  @Input({ required: true }) mainText!: string;
  @Input({ required: true }) buttonText!: string;
  @Input({ required: true }) routerLink!: string;
}
