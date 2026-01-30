describe('Edit Session Component', () => {
  // Données de test
  const mockSession = {
    id: 1,
    name: 'Yoga session',
    description: 'A relaxing yoga session for beginners',
    date: '2026-02-15T00:00:00.000Z',
    teacher_id: 1,
    users: [1, 2, 3],
    createdAt: '2026-01-01T00:00:00.000Z',
    updatedAt: '2026-01-20T00:00:00.000Z'
  };

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

  const updatedSessionData = {
    name: 'Advanced Yoga Session',
    date: '2026-03-20',
    teacher_id: 2,
    description: 'An updated description for advanced practitioners'
  };

  const updatedSession = {
    id: 1,
    ...updatedSessionData,
    date: '2026-03-20T00:00:00.000Z',
    users: [1, 2, 3],
    createdAt: '2026-01-01T00:00:00.000Z',
    updatedAt: '2026-01-30T00:00:00.000Z'
  };

  const mockSessions = [mockSession];

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

    cy.intercept('GET', '/api/session/1', {
      statusCode: 200,
      body: mockSession
    }).as('getSession');

    cy.intercept('GET', '/api/teacher', {
      statusCode: 200,
      body: mockTeachers
    }).as('getTeachers');
  });

  /**
   * TEST 1: La session est modifiée avec succès
   */
  describe('Test 1 - Session update', () => {
    it('should successfully update an existing session', () => {
      // Intercepter la mise à jour
      cy.intercept('PUT', '/api/session/1', {
        statusCode: 200,
        body: updatedSession
      }).as('updateSession');

      // Se connecter en tant qu'admin
      loginAsAdmin();
      cy.wait('@getSessions');

      // Cliquer sur le bouton Edit de la première session
      cy.contains('button', 'Edit').click();

      // Vérifier la navigation vers /sessions/update/1
      cy.url().should('include', '/sessions/update/1');

      // Attendre le chargement des données
      cy.wait('@getSession');
      cy.wait('@getTeachers');

      // Vérifier que le titre est "Update session"
      cy.get('h1').should('contain', 'Update session');

      // Vérifier que les champs sont pré-remplis avec les données existantes
      cy.get('input[formControlName=name]').should('have.value', mockSession.name);
      cy.get('input[formControlName=date]').should('have.value', '2026-02-15');
      cy.get('textarea[formControlName=description]').should('have.value', mockSession.description);

      // Modifier les champs
      cy.get('input[formControlName=name]').clear().type(updatedSessionData.name);
      cy.get('input[formControlName=date]').clear().type(updatedSessionData.date);
      
      // Changer le professeur
      cy.get('mat-select[formControlName=teacher_id]').click();
      cy.get('mat-option').last().click();
      
      // Modifier la description
      cy.get('textarea[formControlName=description]').clear().type(updatedSessionData.description);

      // Vérifier que le bouton Save est activé
      cy.get('button[type=submit]').should('not.be.disabled');

      // Soumettre le formulaire
      cy.get('button[type=submit]').click();

      // Vérifier que la requête de mise à jour a été envoyée
      cy.wait('@updateSession').then((interception) => {
        expect(interception.request.body.name).to.equal(updatedSessionData.name);
        expect(interception.request.body.teacher_id).to.equal(updatedSessionData.teacher_id);
        expect(interception.request.body.description).to.equal(updatedSessionData.description);
      });

      // Vérifier l'affichage du snackbar de confirmation
      cy.contains('Session updated !').should('be.visible');

      // Vérifier la redirection vers /sessions
      cy.url().should('include', '/sessions');
      cy.url().should('not.include', '/update');
    });

    it('should load existing session data in the form', () => {
      loginAsAdmin();
      cy.wait('@getSessions');
      cy.contains('button', 'Edit').click();

      cy.wait('@getSession');
      cy.wait('@getTeachers');

      // Vérifier que tous les champs sont pré-remplis
      cy.get('input[formControlName=name]').should('have.value', mockSession.name);
      cy.get('input[formControlName=date]').should('have.value', '2026-02-15');
      cy.get('textarea[formControlName=description]').should('have.value', mockSession.description);
      
      // Vérifier que le professeur est pré-sélectionné
      cy.get('mat-select[formControlName=teacher_id]').should('contain.text', '');
    });
  });

  /**
   * TEST 2: Affichage d'erreur en absence d'un champ obligatoire
   */
  describe('Test 2 - Validation errors for required fields', () => {
    beforeEach(() => {
      loginAsAdmin();
      cy.wait('@getSessions');
      cy.contains('button', 'Edit').click();
      cy.wait('@getSession');
      cy.wait('@getTeachers');
    });

    it('should display error when name is cleared', () => {
      // Effacer le nom
      cy.get('input[formControlName=name]').clear();

      // Le bouton Save doit être désactivé
      cy.get('button[type=submit]').should('be.disabled');

      // Essayer de taper puis effacer pour déclencher la validation
      cy.get('input[formControlName=name]').type('test').clear().blur();
      
      // Le bouton doit rester désactivé
      cy.get('button[type=submit]').should('be.disabled');
    });

    it('should display error when date is cleared', () => {
      // Effacer la date
      cy.get('input[formControlName=date]').clear();

      // Le bouton Save doit être désactivé
      cy.get('button[type=submit]').should('be.disabled');
    });

    it('should display error when description is cleared', () => {
      // Effacer la description
      cy.get('textarea[formControlName=description]').clear();

      // Le bouton Save doit être désactivé
      cy.get('button[type=submit]').should('be.disabled');
    });

    it('should display error when teacher is cleared', () => {
      // Effacer tous les champs requis un par un et vérifier
      cy.get('input[formControlName=name]').clear();
      cy.get('button[type=submit]').should('be.disabled');

      // Remettre le nom
      cy.get('input[formControlName=name]').type('Test');
      
      // Effacer la date
      cy.get('input[formControlName=date]').clear();
      cy.get('button[type=submit]').should('be.disabled');
    });

    it('should enable submit button when all required fields are valid', () => {
      // Modifier un champ pour déclencher la validation
      cy.get('input[formControlName=name]').clear().type('Updated Session');

      // Tous les autres champs sont déjà remplis, le bouton doit être activé
      cy.get('button[type=submit]').should('not.be.disabled');
    });

    it('should not submit form when fields are invalid', () => {
      // Intercepter pour vérifier qu'aucune requête n'est envoyée
      cy.intercept('PUT', '/api/session/1').as('updateSession');

      // Effacer le nom
      cy.get('input[formControlName=name]').clear();

      // Le bouton est désactivé, donc ne peut pas être cliqué
      cy.get('button[type=submit]').should('be.disabled');

      // Vérifier qu'aucune requête n'a été envoyée
      cy.get('@updateSession.all').should('have.length', 0);
    });
  });


});