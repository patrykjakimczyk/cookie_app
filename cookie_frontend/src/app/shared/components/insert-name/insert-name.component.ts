import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgModel } from '@angular/forms';
import { InsertNameType } from './insert-name.type';

@Component({
  selector: 'app-insert-name',
  templateUrl: './insert-name.component.html',
  styleUrls: ['./insert-name.component.scss'],
})
export class InsertNameComponent {
  @Input({ required: true }) regex!: RegExp;
  @Input() nameTaken?: boolean;
  @Input({ required: true }) type!: InsertNameType;
  @Output() insertedNameEmitter = new EventEmitter<string>();
  protected insertedName = '';

  emitValue(name: NgModel) {
    if (!name.valid) {
      return;
    }

    this.insertedNameEmitter.emit(name.value);
  }
}
