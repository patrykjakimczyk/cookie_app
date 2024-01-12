import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditShoppingListProductsElemComponent } from './edit-shopping-list-products-elem.component';

describe('EditShoppingListProductsElemComponent', () => {
  let component: EditShoppingListProductsElemComponent;
  let fixture: ComponentFixture<EditShoppingListProductsElemComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [EditShoppingListProductsElemComponent]
    });
    fixture = TestBed.createComponent(EditShoppingListProductsElemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
