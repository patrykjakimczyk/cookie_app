import { Component, Input } from '@angular/core';
import { PreviewCardType } from './previev-card-type';

@Component({
  selector: 'app-preview-card',
  templateUrl: './preview-card.component.html',
  styleUrls: ['./preview-card.component.scss'],
})
export class PreviewCardComponent {
  @Input({ required: true }) id!: number;
  @Input({ required: true }) secondId!: number;
  @Input({ required: true }) name!: string;
  @Input({ required: false }) creatorName: string | undefined;
  @Input({ required: false }) groupName: string | undefined;
  @Input({ required: true }) number!: number;
  @Input({ required: true }) type!: PreviewCardType;
}
