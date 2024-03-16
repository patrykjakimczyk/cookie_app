import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ShoppingListProductsElemComponent } from './shopping-list-products-elem.component';

describe('ShoppingListProductsElemComponent', () => {
  let component: ShoppingListProductsElemComponent;
  let fixture: ComponentFixture<ShoppingListProductsElemComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ShoppingListProductsElemComponent]
    });
    fixture = TestBed.createComponent(ShoppingListProductsElemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
