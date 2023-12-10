import { Component, Input } from '@angular/core';
import { PreviewCardType } from './previev-card-type';

@Component({
  selector: 'app-preview-card',
  templateUrl: './preview-card.component.html',
  styleUrls: ['./preview-card.component.scss'],
})
export class PreviewCardComponent {
  @Input({ required: true }) id!: number;
  @Input({ required: true }) name!: string;
  @Input({ required: true }) creatorName!: string;
  @Input({ required: true }) nrOfUsers!: number;
  @Input({ required: true }) type!: PreviewCardType;
}
