/// <reference types="cypress" />
describe('Login spec', () => {
  beforeEach(() => {
    cy.visit('/login');
  });

  describe('Successful login', () => {
    it('should successfully login with valid credentials', () => {
        cy.intercept('POST', '/api/auth/login', {
          body: {
            id: 1,
            username: 'userName',
            firstName: 'firstName',
            lastName: 'lastName',
            admin: true
          },
        })

        cy.intercept(
          {
            method: 'GET',
            url: '/api/session',
          },
          []).as('session')

        cy.get('input[formControlName=email]').type("yoga@studio.com")
        cy.get('input[formControlName=password]').type(`${"test!1234"}{enter}{enter}`)

        cy.url().should('include', '/sessions')
      });
    });

describe('Login error - wrong credentials', () => {
        it('Should show error if login or password is wrong', () => {


          cy.intercept('POST', '/api/auth/login', {
            statusCode: 401,
            body: {
              message: 'Invalid credentials'
            }
          }).as('loginFail')

          cy.get('input[formControlName="email"]').type('FauxEmail@email.com')
          cy.get('input[formControlName="password"]').type('fauxpassword')

          cy.get('button[type="submit"]').click()

          cy.wait('@loginFail')


          cy.url().should('include', '/login')


          cy.contains('An error occurred').should('be.visible')
        });
      });

    describe('Form validation - required fields', () => {
      it('should display error when email is missing', () => {
        cy.get('input[formControlName="password"]').type('password123');

        cy.get('button[type="submit"]').should('be.disabled');

        cy.get('input[formControlName="email"]').focus().blur();

        cy.get('input[formControlName="email"]')
          .closest('mat-form-field')
          .should('have.class', 'mat-form-field-invalid');
      });

      it('should display error when password is missing', () => {
        cy.get('input[formControlName="email"]').type('test@test.com');

        cy.get('button[type="submit"]').should('be.disabled');

        cy.get('input[formControlName="password"]').focus().blur();

        cy.get('input[formControlName="password"]')
          .closest('mat-form-field')
          .should('have.class', 'mat-form-field-invalid');
      });

      it('should display error when all fields are empty', () => {
        cy.get('input[formControlName="email"]').focus().blur();
        cy.get('input[formControlName="password"]').focus().blur();

        cy.get('button[type="submit"]').should('be.disabled');

        cy.get('mat-form-field.mat-form-field-invalid').should('have.length', 2);
      });
    });
  });