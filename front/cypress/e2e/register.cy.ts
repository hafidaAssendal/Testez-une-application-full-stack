
/// <reference types="cypress" />
describe('Register spec', () => {
  beforeEach(() => {
    cy.visit('/register');
  });

  it('should display the register form', () => {
    cy.get('input[formControlName="email"]').should('be.visible');
    cy.get('input[formControlName="firstName"]').should('be.visible');
    cy.get('input[formControlName="lastName"]').should('be.visible');
    cy.get('input[formControlName="password"]').should('be.visible');
    cy.get('button[type="submit"]').should('be.visible');
  });

  describe('Form validation - required fields', () => {
    it('should display error when email is missing', () => {
      // Remplir tous les champs sauf email
      cy.get('input[formControlName="firstName"]').type('John');
      cy.get('input[formControlName="lastName"]').type('Doe');
      cy.get('input[formControlName="password"]').type('password123');

      // Vérifier que le bouton est désactivé
      cy.get('button[type="submit"]').should('be.disabled');

      // Vérifier que l'erreur s'affiche pour email (toucher le champ puis sortir)
      cy.get('input[formControlName="email"]').focus().blur();

      cy.get('input[formControlName="email"]')
        .closest('mat-form-field')
        .should('have.class', 'mat-form-field-invalid');
    });

    it('should display error when firstName is missing', () => {
      cy.get('input[formControlName="email"]').type('test@test.com');
      cy.get('input[formControlName="lastName"]').type('Doe');
      cy.get('input[formControlName="password"]').type('password123');

      // Vérifier que le bouton est désactivé
      cy.get('button[type="submit"]').should('be.disabled');

      // Toucher le champ firstName puis sortir pour déclencher la validation
      cy.get('input[formControlName="firstName"]').focus().blur();

      cy.get('input[formControlName="firstName"]')
        .closest('mat-form-field')
        .should('have.class', 'mat-form-field-invalid');
    });

    it('should display error when lastName is missing', () => {
      cy.get('input[formControlName="email"]').type('test@test.com');
      cy.get('input[formControlName="firstName"]').type('John');
      cy.get('input[formControlName="password"]').type('password123');

      // Vérifier que le bouton est désactivé
      cy.get('button[type="submit"]').should('be.disabled');

      // Toucher le champ lastName puis sortir pour déclencher la validation
      cy.get('input[formControlName="lastName"]').focus().blur();

      cy.get('input[formControlName="lastName"]')
        .closest('mat-form-field')
        .should('have.class', 'mat-form-field-invalid');
    });

    it('should display error when password is missing', () => {
      cy.get('input[formControlName="email"]').type('test@test.com');
      cy.get('input[formControlName="firstName"]').type('John');
      cy.get('input[formControlName="lastName"]').type('Doe');

      // Vérifier que le bouton est désactivé
      cy.get('button[type="submit"]').should('be.disabled');

      // Toucher le champ password puis sortir pour déclencher la validation
      cy.get('input[formControlName="password"]').focus().blur();

      cy.get('input[formControlName="password"]')
        .closest('mat-form-field')
        .should('have.class', 'mat-form-field-invalid');
    });

    it('should display error when all fields are empty', () => {
      // Toucher chaque champ puis sortir pour déclencher toutes les validations
      cy.get('input[formControlName="email"]').focus().blur();
      cy.get('input[formControlName="firstName"]').focus().blur();
      cy.get('input[formControlName="lastName"]').focus().blur();
      cy.get('input[formControlName="password"]').focus().blur();

      // Vérifier que le bouton est désactivé
      cy.get('button[type="submit"]').should('be.disabled');

      // Vérifier que tous les champs sont invalides
      cy.get('mat-form-field.mat-form-field-invalid').should('have.length', 4);
    });
  });

  describe('Successful registration', () => {
    it('should successfully register with valid data and navigate to login', () => {
      // Intercepter l'appel API
      cy.intercept('POST', '**/api/auth/register', {
        statusCode: 200,
        body: {}
      }).as('registerRequest');

      // Remplir le formulaire avec des données valides
      cy.get('input[formControlName="email"]').type('test@test.com');
      cy.get('input[formControlName="firstName"]').type('John');
      cy.get('input[formControlName="lastName"]').type('Doe');
      cy.get('input[formControlName="password"]').type('password123');

      // Soumettre le formulaire
      cy.get('button[type="submit"]').click();

      // Vérifier que la requête a été envoyée
      cy.wait('@registerRequest').its('request.body').should('deep.equal', {
        email: 'test@test.com',
        firstName: 'John',
        lastName: 'Doe',
        password: 'password123'
      });

      // Vérifier la redirection vers /login
      cy.url().should('include', '/login');
    });
  });

  describe('Registration error ', () => {
    it('should display error message when registration fails', () => {
      // Intercepter l'appel API avec une erreur
      cy.intercept('POST', '**/api/auth/register', {
        statusCode: 400,
        body: { message: 'Email already exists' }
      }).as('registerRequestError');

      // Remplir le formulaire
      cy.get('input[formControlName="email"]').type('existing@test.com');
      cy.get('input[formControlName="firstName"]').type('John');
      cy.get('input[formControlName="lastName"]').type('Doe');
      cy.get('input[formControlName="password"]').type('password123');

      // Soumettre le formulaire
      cy.get('button[type="submit"]').click();

      // Vérifier que la requête a été envoyée
      cy.wait('@registerRequestError');

      // Vérifier que le message d'erreur s'affiche
      cy.get('.error').should('be.visible');
      // Ou selon votre implémentation HTML:
      // cy.contains('An error occurred').should('be.visible');

      // Vérifier qu'on reste sur la page register
      cy.url().should('include', '/register');
    });
  });


});