import { Component, EventEmitter, Input, Output } from '@angular/core';
import { PreviewCardType } from './preview-card-type';

@Component({
  selector: 'app-preview-card',
  templateUrl: './preview-card.component.html',
  styleUrls: ['./preview-card.component.scss'],
})
export class PreviewCardComponent {
  @Input({ required: true }) firstColumn!: string;
  @Input({ required: true }) secondColumn!: string;
  @Input({ required: true }) thirdColumn!: string;
  @Input({ required: false }) firstButtonText: string | undefined;
  @Input({ required: true }) secondButtonText!: string;
  @Output() firstButtonClicked = new EventEmitter<void>();
  @Output() secondButtonClicked = new EventEmitter<void>();

  firstButtonClick() {
    this.firstButtonClicked.emit();
  }

  secondButtonClick() {
    this.secondButtonClicked.emit();
  }
}
