/// <reference types="cypress" />
describe('Create Session Component', () => {
  // Données de test
  const mockTeachers = [
    {
      id: 1,
      firstName: 'Margot',
      lastName: 'Delahaye',
      createdAt: '2025-01-01T00:00:00.000Z',
      updatedAt: '2025-01-01T00:00:00.000Z'
    },
    {
      id: 2,
      firstName: 'Hélène',
      lastName: 'Thiercé',
      createdAt: '2025-01-01T00:00:00.000Z',
      updatedAt: '2025-01-01T00:00:00.000Z'
    }
  ];

  const adminUser = {
    token: 'admin-token-123',
    type: 'Bearer',
    id: 1,
    username: 'admin@test.com',
    firstName: 'Admin',
    lastName: 'User',
    admin: true
  };

  const newSession = {
    name: 'Advanced Yoga',
    date: '2026-03-15',
    teacher_id: 1,
    description: 'An advanced yoga session for experienced practitioners'
  };

  const createdSession = {
    id: 10,
    ...newSession,
    date: '2026-03-15T00:00:00.000Z',
    users: [],
    createdAt: '2026-01-30T00:00:00.000Z',
    updatedAt: '2026-01-30T00:00:00.000Z'
  };

  const mockSessions = [
    {
      id: 1,
      name: 'Yoga session',
      description: 'A relaxing yoga session',
      date: '2026-02-15T00:00:00.000Z',
      teacher_id: 1,
      users: [1, 2, 3],
      createdAt: '2026-01-01T00:00:00.000Z',
      updatedAt: '2026-01-20T00:00:00.000Z'
    }
  ];

  /**
   * Helper function pour se connecter en tant qu'admin
   */
  const loginAsAdmin = () => {
    cy.visit('/login');
    cy.get('input[formControlName=email]').type(adminUser.username);
    cy.get('input[formControlName=password]').type('test!1234{enter}{enter}');
    cy.wait('@login');
  };

  beforeEach(() => {
    // Intercepter tous les appels API
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: adminUser
    }).as('login');

    cy.intercept('GET', '/api/session', {
      statusCode: 200,
      body: mockSessions
    }).as('getSessions');

    cy.intercept('GET', '/api/teacher', {
      statusCode: 200,
      body: mockTeachers
    }).as('getTeachers');
  });

  /**
   * TEST 1: La session est créée avec succès
   */
  describe('Test 1 - Session creation', () => {
    it('should successfully create a new session', () => {
      // Intercepter la création de session
      cy.intercept('POST', '/api/session', {
        statusCode: 200,
        body: createdSession
      }).as('createSession');

      // Se connecter en tant qu'admin
      loginAsAdmin();

      // Attendre le chargement de la liste
      cy.wait('@getSessions');

      // Cliquer sur le bouton Create
      cy.contains('button', 'Create').click();

      // Vérifier la navigation vers /sessions/create
      cy.url().should('include', '/sessions/create');

      // Attendre le chargement des professeurs
      cy.wait('@getTeachers');

      // Vérifier que le titre est "Create session"
      cy.get('h1').should('contain', 'Create session');

      // Remplir le formulaire
      cy.get('input[formControlName=name]').type(newSession.name);
      cy.get('input[formControlName=date]').type(newSession.date);
      
      // Sélectionner un professeur
      cy.get('mat-select[formControlName=teacher_id]').click();
      cy.get('mat-option').first().click();
      
      // Remplir la description
      cy.get('textarea[formControlName=description]').type(newSession.description);

      // Vérifier que le bouton Save est activé
      cy.get('button[type=submit]').should('not.be.disabled');

      // Soumettre le formulaire
      cy.get('button[type=submit]').click();

      // Vérifier que la requête de création a été envoyée
      cy.wait('@createSession').then((interception) => {
        expect(interception.request.body.name).to.equal(newSession.name);
        expect(interception.request.body.teacher_id).to.equal(newSession.teacher_id);
        expect(interception.request.body.description).to.equal(newSession.description);
      });

      // Vérifier l'affichage du snackbar de confirmation
      cy.contains('Session created !').should('be.visible');

      // Vérifier la redirection vers /sessions
      cy.url().should('include', '/sessions');
    });
  });

  /**
   * TEST 2: Affichage d'erreur en absence d'un champ obligatoire
   */
  describe('Test 2 - Validation errors for required fields', () => {
    beforeEach(() => {
      loginAsAdmin();
      cy.wait('@getSessions');
      cy.contains('button', 'Create').click();
      cy.wait('@getTeachers');
    });

    it('should display error when name is missing', () => {
      // Ne pas remplir le nom
      cy.get('input[formControlName=date]').type('2026-03-15');
      cy.get('mat-select[formControlName=teacher_id]').click();
      cy.get('mat-option').first().click();
      cy.get('textarea[formControlName=description]').type('Test description');

      // Le bouton Save doit être désactivé
      cy.get('button[type=submit]').should('be.disabled');
    });

    it('should display error when date is missing', () => {
      cy.get('input[formControlName=name]').type('Test Session');
      // Ne pas remplir la date
      cy.get('mat-select[formControlName=teacher_id]').click();
      cy.get('mat-option').first().click();
      cy.get('textarea[formControlName=description]').type('Test description');

      // Le bouton Save doit être désactivé
      cy.get('button[type=submit]').should('be.disabled');
    });

    it('should display error when teacher is missing', () => {
      cy.get('input[formControlName=name]').type('Test Session');
      cy.get('input[formControlName=date]').type('2026-03-15');
      // Ne pas sélectionner de professeur
      cy.get('textarea[formControlName=description]').type('Test description');

      // Le bouton Save doit être désactivé
      cy.get('button[type=submit]').should('be.disabled');
    });

    it('should display error when description is missing', () => {
      cy.get('input[formControlName=name]').type('Test Session');
      cy.get('input[formControlName=date]').type('2026-03-15');
      cy.get('mat-select[formControlName=teacher_id]').click();
      cy.get('mat-option').first().click();
      // Ne pas remplir la description

      // Le bouton Save doit être désactivé
      cy.get('button[type=submit]').should('be.disabled');
    });

    it('should enable submit button only when all required fields are filled', () => {
      // Initialement, le bouton doit être désactivé
      cy.get('button[type=submit]').should('be.disabled');

      // Remplir tous les champs
      cy.get('input[formControlName=name]').type('Test Session');
      cy.get('input[formControlName=date]').type('2026-03-15');
      cy.get('mat-select[formControlName=teacher_id]').click();
      cy.get('mat-option').first().click();
      cy.get('textarea[formControlName=description]').type('Test description');

      // Le bouton Save doit maintenant être activé
      cy.get('button[type=submit]').should('not.be.disabled');
    });
  });

 
});